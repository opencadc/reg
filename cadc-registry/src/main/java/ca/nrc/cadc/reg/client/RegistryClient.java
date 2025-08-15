/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2022.                            (c) 2022.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.reg.client;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.net.ResourceNotFoundException;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.CapabilitiesReader;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Interface;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.util.InvalidConfigException;
import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.PropertiesReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * A very simple caching IVOA Registry client. All the lookups done by this client use properties
 * files served from a well-known URL. Requires either baseURL configuration in cadc-registry.properties
 * OR a java system property <code>ca.nrc.cadc.reg.client.RegistryClient.host</code>
 * set to the hostname of the registry service (hard-coded: https protocol and a service named "reg").
 * The config file takes priority; the system property is intended for use by developers to lookup
 * local services in their own reg service for testing purposes.
 *
 * @author pdowler
 */
public class RegistryClient {

    private static Logger log = Logger.getLogger(RegistryClient.class);

    private static final String HOST_PROPERTY_KEY = RegistryClient.class.getName() + ".host";
    
    private static final String CONFIG_BASE_URL_KEY = RegistryClient.class.getName() + ".baseURL";

    public enum Query {
        APPLICATIONS("applications"),
        CAPABILITIES("resource-caps");
        
        private String value;
        
        private Query(String s) {
            this.value = s;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    static final String DEFAULT_CONFIG_FILE_NAME = "cadc-registry.properties";
    
    // version the cache dir so we can increment when we have incompatible cache structure
    // 1.5 because we now put all reg lookups under a capsDomain
    static final String CONFIG_CACHE_DIR = "cadc-registry-1.5";
    
    private static final String FILE_SEP = System.getProperty("file.separator");

    // fully qualified type value (see CapabilitiesReader)
    private static final URI DEFAULT_ITYPE = Standards.INTERFACE_PARAM_HTTP;

    private final List<URL> regBaseURLs = new ArrayList<URL>();
    // private String capsDomain;
    private boolean isRegOverride = false;
    private int connectionTimeout = 30000; // millis
    private int readTimeout = 60000;       // millis

    /**
     * Default constructor, using the DEFAULT_CONFIG_FILE_NAME. 
     * 
     */
    public RegistryClient() {
        this(new PropertiesReader(DEFAULT_CONFIG_FILE_NAME));
    }

    /*
     * Parameterised constructor to make testing easier.
     * Needs to be public because the mock tests are in a separate package.
     * @param configFile The configuration file to use.
     * 
     */
    public RegistryClient(final File configFile) {
        this(new PropertiesReader(configFile));
    }

    /*
     * Private constructor called by the others.
     * @param propReader A PropertiesReader for the configuration file.
     * 
     */
    private RegistryClient(final PropertiesReader propReader) {
        MultiValuedProperties mvp = propReader.getAllProperties();

        for (String str : mvp.getProperty(CONFIG_BASE_URL_KEY))
            {
            try {
                if (str.endsWith("/")) {
                    str= str.substring(0, str.length() - 1);
                    }
                this.regBaseURLs.add(new URL(str));
                }
            catch (MalformedURLException ex)
                {
                throw new InvalidConfigException(CONFIG_BASE_URL_KEY  + " = " + str + " is not a valid URL", ex);
                }
            }

        if (this.regBaseURLs.isEmpty())
            {
            try {
                String hostP = System.getProperty(HOST_PROPERTY_KEY);
                log.debug("     host: " + hostP);
                if (hostP != null) {
                    this.regBaseURLs.add(new URL("https://" + hostP + "/reg"));
                    this.isRegOverride = true;
                    }
                }
            catch (MalformedURLException e) {
                log.error("Error transforming resource-caps URL", e);
                throw new RuntimeException(e);
                }
            }
        }

    /**
     * HTTP connection timeout in milliseconds (default: 30000).
     * 
     * @param connectionTimeout in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * HTTP read timeout in milliseconds (default: 60000).
     * 
     * @param readTimeout in milliseconds
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    /**
     * Find out if registry lookup URL was modified by a system property. This
     * typically indicates that the code is running in a development/test environment.
     *
     * @return true if lookup is modified, false if configured
     */
    public boolean isRegistryLookupOverride() {
        return isRegOverride;
    }

    /**
     * Backwards compatibility/convenience: get the capabilities URL for the specified service.
     * 
     * @param resourceID of a service that implements VOSI-capabilities
     * @return capabilities URL
     * @throws IOException      local cache file(s) cannot be read or written
     * @throws ca.nrc.cadc.net.ResourceNotFoundException if the resourceID cannot be found in the registry 
     */
    public URL getAccessURL(URI resourceID) throws IOException, ResourceNotFoundException {
        return getAccessURL(Query.CAPABILITIES, resourceID);
    }
    
    /**
     * Get the accessURL for the resourceID or standardID from the specified query.
     *
     * @param queryName  name of the canned query: QUERY_CAPABILITIES or QUERY_APPLICATIONS
     * @param uri        a resourceID (for QUERY_CAPABILITIES) or a standardID (for QUERY_APPLICATIONS)
     * @return URL       the location of the resource
     * @throws ca.nrc.cadc.net.ResourceNotFoundException if the resourceID cannot be found, check the Exception cause for more details
     */
    public URL getAccessURL(Query queryName, URI uri) throws IOException, ResourceNotFoundException {

        if (regBaseURLs.isEmpty()) {
            throw new IllegalStateException("Registry base URL list is empty");
        }

        List<Exception> exceptions = new ArrayList<Exception>();
        
        for (URL regBaseURL : regBaseURLs) {
            
            try {
                log.debug("registry base URL [" + regBaseURL + "]");
                File queryCacheFile = getQueryCacheFile(regBaseURL, queryName);
                log.debug("resource-caps cache file [" + queryCacheFile + "]");
    
                URL queryURL = new URL(regBaseURL + "/" + queryName.getValue());
                log.debug("query URL [" + queryURL + "]");
    
                CachingFile cachedCapSource = new CachingFile(queryCacheFile, queryURL);
                cachedCapSource.setConnectionTimeout(connectionTimeout);
                cachedCapSource.setReadTimeout(readTimeout);
    
                String map = null ;
                try {
                    map = cachedCapSource.getContent();
                } catch (IOException e) {
                    throw new RuntimeException(
                        "CachingFile.getContent from registry [" + regBaseURL + "] failed with IOException [" + e.getMessage() + "]",
                        e
                    );
                }
                InputStream mapStream = new ByteArrayInputStream(map.getBytes(StandardCharsets.UTF_8));
                MultiValuedProperties mvp = new MultiValuedProperties();
                try {
                    mvp.load(mapStream);
                } catch (IOException e) {
                    throw new RuntimeException(
                        "MultiValuedProperties.load from registry [" + regBaseURL + "] failed with IOException [" + queryURL + "][" + e.getMessage() + "]",
                        e
                    );
                }
                List<String> values = mvp.getProperty(uri.toString());
                if ((values == null) || (values.isEmpty())) {
                    throw new RuntimeException(
                        "MultiValuedProperties.getProperty from registry [" + regBaseURL + "] for [" + uri.toString() + "] returned null or empty list"
                    );
                }
                if (values.size() > 1) {
                    throw new RuntimeException(
                        "MultiValuedProperties.getProperty from registry [" + regBaseURL + "] for [" + uri.toString() + "] returned more than one value"
                    );
                }
                try {
                    return new URL(
                        values.get(0)
                    );
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(
                        "Parsing accessURL [" + values.get(0) + "] from registry [" + regBaseURL + "] threw MalformedURLException [" + e.getMessage() + "]",
                        e
                    );
                }
            }
            catch (Exception e) {
                log.warn("Exception querying [" + regBaseURL + "] for [" + queryName.getValue() + "] message [" + e.getMessage() + "]");
                exceptions.add(e);
                continue;
            }
        }

        throw new ResourceNotFoundException(
            "Failed to find registry resource for [" + queryName.getValue() + "][" + uri + "]",
            ((exceptions.isEmpty()) ? null : exceptions.get(0))
        ); 
    }

    /**
     * Get the capabilities object for the resource identified by resourceID.
     *
     * @param resourceID Identifies the resource.
     * @return The associated capabilities object.
     *
     * @throws IOException If the capabilities could not be determined.
     * @throws ca.nrc.cadc.net.ResourceNotFoundException if the resourceID cannot be found in the registry
     */
    public Capabilities getCapabilities(URI resourceID) throws IOException, ResourceNotFoundException {
        if (resourceID == null) {
            String msg = "Input parameter (resourceID) should not be null";
            throw new IllegalArgumentException(msg);
        }

        final URL serviceCapsURL = getAccessURL(Query.CAPABILITIES, resourceID);

        log.debug("Service capabilities URL: " + serviceCapsURL);

        File capabilitiesFile = this.getCapabilitiesCacheFile(serviceCapsURL, resourceID);
        CachingFile cachedCapabilities = new CachingFile(capabilitiesFile, serviceCapsURL);
        cachedCapabilities.setConnectionTimeout(connectionTimeout);
        cachedCapabilities.setReadTimeout(readTimeout);
        String xml = cachedCapabilities.getContent();
        CapabilitiesReader capReader = new CapabilitiesReader();
        return capReader.read(xml);
    }

    /**
     * Find the service URL for the service registered under the specified base resource
     * identifier and using the specified authentication method. The identifier must be an
     * IVOA identifier (e.g. with URI scheme "ivo"). This method uses the default
     * interface type ParamHTTP defined in VOResource and returns the first matching
     * interface.
     *
     * @param resourceIdentifier resource identifier, e.g. ivo://cadc.nrc.ca/tap
     * @param standardID         IVOA standard identifier, e.g. ivo://ivo.net/std/TAP
     * @param authMethod         authentication method to be used
     * @return service URL or null if a matching interface was not found
     */
    public URL getServiceURL(final URI resourceIdentifier, final URI standardID, final AuthMethod authMethod) {
        return getServiceURL(resourceIdentifier, standardID, authMethod, DEFAULT_ITYPE);
    }

    /**
     * Find the service URL for the service registered under the specified base resource
     * identifier and using the specified authentication method. The identifier must be an
     * IVOA identifier (e.g. with URI scheme "ivo"). This method returns the first matching
     * interface.
     *
     * @param regBaseURL the registry endpoint URL
     * @param resourceID        ID of the resource to lookup.
     * @param standardID                The standard ID of the resource to look up.  Indicates the specific purpose of
     *                                  the resource to get a URL for.
     * @param authMethod                What Authentication method to use (certificate, cookie, etc.)
     * @param interfaceType             Interface type indicating how to access the resource (e.g. HTTP).  See IVOA
     *                                  resource identifiers
     * @return service URL or null if a matching interface was not found
     */
    public URL getServiceURL(final URI resourceID, final URI standardID, final AuthMethod authMethod, URI interfaceType) {
        if (resourceID == null || standardID == null || interfaceType == null) {
            String msg = "No input parameters should be null";
            throw new IllegalArgumentException(msg);
        }

        URL url = null;
        log.debug("resourceIdentifier=" + resourceID
                          + ", standardID=" + standardID
                          + ", authMethod=" + authMethod
                          + ", interfaceType=" + interfaceType);
        Capabilities caps = null;
        try {
            caps = this.getCapabilities(resourceID);
        } catch (ResourceNotFoundException ex) {
            log.warn("getCapabilities: " + ex);
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Could not obtain service URL", e);
        }

        // locate the associated capability
        Capability cap = caps.findCapability(standardID);

        if (cap != null) {
            // locate the associated interface, throws RuntimeException if more than
            // one interface match
            Interface intf = cap.findInterface(authMethod, interfaceType);

            if (intf != null) {
                url = intf.getAccessURL().getURL();
            }
        }

        // return associated access URL, mangle it if necessary
        return url;
    }

    File getQueryCacheFile(final URL queryURL, Query queryName) {
        String baseCacheDir = getBaseCacheDirectory();
        baseCacheDir += FILE_SEP + this.getCapsDomain(queryURL);
        String path = FILE_SEP + queryName.getValue();
        log.debug("getQueryCacheFile [" + path + "] in dir [" + baseCacheDir + "]");
        File file = new File(baseCacheDir + path);
        return file;
    }

    private File getCapabilitiesCacheFile(final URL regBaseURL, URI resourceID) {
        String baseCacheDir = getBaseCacheDirectory();
        String resourceCacheDir = baseCacheDir + resourceID.getAuthority();
        resourceCacheDir = baseCacheDir + FILE_SEP + this.getCapsDomain(regBaseURL) + FILE_SEP + resourceID.getAuthority();
        String path = resourceID.getPath() + FILE_SEP + "capabilities.xml";
        log.debug("getCapabilitiesCacheFile [" + path + "] in dir [" + resourceCacheDir + "]");
        File file = new File(resourceCacheDir, path);
        return file;
    }

    protected String getBaseCacheDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String userName = System.getProperty("user.name");
        if (tmpDir == null) {
            throw new RuntimeException("No tmp system dir defined.");
        }
        String baseCacheDir = null;
        if (userName == null) {
            baseCacheDir = tmpDir + FILE_SEP + CONFIG_CACHE_DIR;
        } else {
            baseCacheDir = tmpDir + FILE_SEP + userName + FILE_SEP + CONFIG_CACHE_DIR;
        }
        log.debug("Base cache dir: " + baseCacheDir);
        return baseCacheDir;
    }

    // for test access
    List<URL> getRegistryBaseURLs() {
        return regBaseURLs;
    }

    String getCapsDomain(final URL domainQueryURL) {
        return domainQueryURL.getHost();
    }

    /**
     * Delete the cache directory.
     * Added to make testing more predictable.
     * @throws IOException
     * 
     */
    public void deleteCache()
        throws IOException {
        deleteDirectory(
            Paths.get(
                this.getBaseCacheDirectory()
            )
        );
    }
    
    /**
     * Recursive directory delete using FileVisitor.
     * https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileVisitor.html
     * @param path
     * @throws IOException
     * 
     */
    public static void deleteDirectory(final Path path)
        throws IOException {
        if (path.toFile().exists()) {
            Files.walkFileTree(
                path,
                new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e)
                        throws IOException {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                        else {
                            throw e;
                        }
                    }
                }
            );        
        }        
    }        
}
