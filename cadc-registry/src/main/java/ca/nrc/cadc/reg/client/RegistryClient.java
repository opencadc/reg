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
import ca.nrc.cadc.util.MultiValuedProperties;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * A very simple caching IVOA Registry client. All the lookups done by this client use properties
 * files served from a well-known URL. 
 * <p>
 * Deployers can set a java system property to force this class to replace the hostname
 * in the resulting URL with an arbitrary hostname. This is useful for testing a specific remote server:
 * </p>
 * <pre>
 * ca.nrc.cadc.reg.client.RegistryClient.host=www.example.com
 * </pre>
 * <p>
 * The <code>ca.nrc.cadc.reg.client.RegistryClient.host</code> property replaces the entire fully-qualified host name
 * with the specified value.
 * </p>
 *
 * @author pdowler
 */
public class RegistryClient {

    private static Logger log = Logger.getLogger(RegistryClient.class);

    private static final String HOST_PROPERTY = RegistryClient.class.getName() + ".host";

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
    
    // version the cache dir so we can increment when we have incompatible cache structure
    static final String CONFIG_CACHE_DIR = "cadc-registry-1.4";
    private static final URL DEFAULT_REG_BASE_URL;
    private static final String FILE_SEP = System.getProperty("file.separator");

    // fully qualified type value (see CapabilitiesReader)
    private static final URI DEFAULT_ITYPE = Standards.INTERFACE_PARAM_HTTP;

    private String hostname;
    private URL regBaseURL;
    private String capsDomain;

    static {
        try {
            DEFAULT_REG_BASE_URL = new URL("https://ws.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg");
        } catch (MalformedURLException e) {
            log.fatal("BUG: hard-coded default URL is malformed", e);
            throw new ExceptionInInitializerError("BUG: hard-coded default URL is malformed: " + e.getMessage());
        }
    }

    public RegistryClient() {
        init(DEFAULT_REG_BASE_URL);
    }

    /**
     * Find out if registry lookup URL was modified by a system property. This
     * typically indicates that the code is running in a development/test environment.
     *
     * @return true if lookup is modified, false if default (production)
     */
    public boolean isRegistryLookupOverride() {
        return !DEFAULT_REG_BASE_URL.equals(regBaseURL);
    }

    private void init(URL origURL) {
        try {
            String hostP = System.getProperty(HOST_PROPERTY);
            log.debug("     host: " + hostP);
            if (hostP != null && this.hostname == null) {
                hostP = hostP.trim();
                if (hostP.length() > 0) {
                    this.hostname = hostP;
                }
            }

            log.debug("Original resourceCapURL: " + origURL);
            this.regBaseURL = mangleHostname(origURL);
            log.debug("Mangled resourceCapURL: " + this.regBaseURL);
            if (!origURL.equals(this.regBaseURL)) {
                capsDomain = "alt-domains/" + this.regBaseURL.getHost();
            }
        } catch (MalformedURLException e) {
            log.error("Error transforming resource-caps URL", e);
            throw new RuntimeException(e);
        }
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
     * @return URL              Location of the resource
     * @throws IOException      If the cache file cannot be read
     * @throws ca.nrc.cadc.net.ResourceNotFoundException if the resourceID cannot be found in the registry
     */
    public URL getAccessURL(Query queryName, URI uri) throws IOException, ResourceNotFoundException {
        File queryCacheFile = getQueryCacheFile(queryName);
        log.debug("Capabilities cache file: " + queryCacheFile);
        URL queryURL = new URL(regBaseURL + "/" + queryName.getValue());
        CachingFile cachedCapSource = new CachingFile(queryCacheFile, queryURL);
        String map = cachedCapSource.getContent();
        InputStream mapStream = new ByteArrayInputStream(map.getBytes(StandardCharsets.UTF_8));
        MultiValuedProperties mvp = new MultiValuedProperties();
        try {
            mvp.load(mapStream);
        } catch (Exception e) {
            throw new RuntimeException("failed to load properties from cache, src=" + queryURL, e);
        }

        List<String> values = mvp.getProperty(uri.toString());
        if (values == null || values.isEmpty()) {
            throw new ResourceNotFoundException("not found: " + uri + " src=" + queryURL);
        }
        if (values.size() > 1) {
            throw new RuntimeException("multiple values for " + uri + " src=" + queryURL);
        }
        try {
            return new URL(values.get(0));
        } catch (MalformedURLException e) {
            throw new RuntimeException("malformed URL for " + uri + " src=" + queryURL, e);
        }
    }

    /**
     * Get the capabilities object for the resource identified
     * by resourceID.
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

        File capabilitiesFile = this.getCapabilitiesCacheFile(resourceID);
        CachingFile cachedCapabilities = new CachingFile(capabilitiesFile, serviceCapsURL);
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
     * @param resourceID        ID of the resource to lookup.
     * @param standardID                The standard ID of the resource to look up.  Indicates the specific purpose of
     *                                  the resource to get a URL for.
     * @param authMethod                What Authentication method to use (certificate, cookie, etc.)
     * @param interfaceType             Interface type indicating how to access the resource (e.g. HTTP).  See IVOA
     *                                  resource identifiers
     * @return service URL or null if a matching interface was not found
     */
    public URL getServiceURL(final URI resourceID, final URI standardID, final AuthMethod authMethod,
                             URI interfaceType) {
        if (resourceID == null || standardID == null || authMethod == null || interfaceType == null) {
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

    File getQueryCacheFile(Query queryName) {
        String baseCacheDir = getBaseCacheDirectory();
        if (this.capsDomain != null) {
            baseCacheDir += FILE_SEP + this.capsDomain;
        }
        String path = FILE_SEP + queryName.getValue();
        log.debug("Caching file [" + path + "] in dir [" + baseCacheDir + "]");
        File file = new File(baseCacheDir + path);
        return file;
    }

    private File getCapabilitiesCacheFile(URI resourceID) {
        String baseCacheDir = getBaseCacheDirectory();
        String resourceCacheDir = baseCacheDir + resourceID.getAuthority();
        if (capsDomain != null) {
            resourceCacheDir = baseCacheDir + capsDomain + FILE_SEP + resourceID.getAuthority();
        }
        String path = resourceID.getPath() + FILE_SEP + "capabilities.xml";
        log.debug("Caching file [" + path + "] in dir [" + resourceCacheDir + "]");
        File file = new File(resourceCacheDir, path);
        return file;
    }

    private String getBaseCacheDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String userName = System.getProperty("user.name");
        if (tmpDir == null) {
            throw new RuntimeException("No tmp system dir defined.");
        }
        String baseCacheDir = null;
        if (userName == null) {
            baseCacheDir = tmpDir + FILE_SEP + CONFIG_CACHE_DIR + FILE_SEP;
        } else {
            baseCacheDir = tmpDir + FILE_SEP + userName + FILE_SEP + CONFIG_CACHE_DIR + FILE_SEP;
        }
        log.debug("Base cache dir: " + baseCacheDir);
        return baseCacheDir;
    }

    public URL mangleHostname(final URL url) throws MalformedURLException {
        URL retURL = url;

        log.debug("mangling URL: " + url);
        if (this.hostname != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(url.getProtocol());
            sb.append("://");

            sb.append(this.hostname);

            int p = url.getPort();
            if (p > 0 && p != url.getDefaultPort()) {
                sb.append(":");
                sb.append(p);
            }

            sb.append(url.getPath());
            retURL = new URL(sb.toString());
        }

        log.debug("mangled URL: " + retURL);
        return retURL;
    }

    public static String getDomain(String hostname) {
        if (hostname == null) {
            return null;
        }
        int dotIndex = hostname.indexOf('.');
        if (dotIndex <= 0) {
            return null;
        }
        if (dotIndex + 1 == hostname.length()) {
            return null;
        }
        return hostname.substring(dotIndex + 1);
    }

    // for test access
    URL getRegistryBaseURL() {
        return regBaseURL;
    }

    String getCapsDomain() {
        return capsDomain;
    }
}
