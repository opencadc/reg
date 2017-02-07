/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2010.                            (c) 2010.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.CapabilitiesReader;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Interface;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.util.MultiValuedProperties;


/**
 * A very simple caching IVOA Registry client. All the lookups done by this client use a properties
 * file named RegistryClient.properties found via the classpath.
 * <p>
 * Note for developers: You can set a system property to force this class to replace the hostname
 * in the resulting URL with the canonical hostname of the local host. This is useful for testing:
 * </p>
 * <pre>
 * ca.nrc.cadc.reg.client.RegistryClient.local=true
 * </pre>
 * <p>
 * Note for developers: You can set a system property to force this class to replace the hostname
 * in the resulting URL with an arbitrary hostname. This is useful for testing a specific remote server:
 * </p>
 * <pre>
 * ca.nrc.cadc.reg.client.RegistryClient.host=www.example.com
 * </pre>
 * <p>
 * or for testing in a special environment:
 * </p>
 * <pre>
 * ca.nrc.cadc.reg.client.RegistryClient.shortHostname=test
 * </pre>
 * <p>
 * The <code>ca.nrc.cadc.reg.client.RegistryClient.host</code> property replaces the entire fully-qualified host name
 * with the specified value. The <code>ca.nrc.cadc.reg.client.RegistryClient.shortHostname</code> property
 * replaces only the hostname and leaves the domain intact; this is useful if you run in multiple domains and have a
 * set of test machines that span domains. The The <code>ca.nrc.cadc.reg.client.RegistryClient.domainMatch</code>
 * property (comma-separated list of domains) can be used to limit hostname modifications to the specified domains; if
 * it is not set, all URLs will be modified.
 * </p>
 *
 * @author pdowler
 */
public class RegistryClient
{
    private static Logger log = Logger.getLogger(RegistryClient.class);

    private static final String LOCAL_PROPERTY = RegistryClient.class.getName() + ".local";
    private static final String HOST_PROPERTY = RegistryClient.class.getName() + ".host";
    private static final String SHORT_HOST_PROPERTY = RegistryClient.class.getName() + ".shortHostname";
    private static final String DOMAIN_MATCH_PROPERTY = RegistryClient.class.getName() + ".domainMatch";

    private static final String CONFIG_CACHE_DIR = "/.config/cadc-registry/";
    private static final URL RESOURCE_CAPS_URL;
    private static final String RESOURCE_CAPS_NAME = "resource-caps";

    private String hostname;
    private String shortHostname;
    private List<String>domainMatch = new ArrayList<String>();
    private URL resourceCapsURL;
    private String capsDomain;

    static
    {
        try
        {
            RESOURCE_CAPS_URL = new URL("http://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg/resource-caps");
        }
        catch (MalformedURLException e)
        {
            log.fatal("BUG: RESOURCE_CAPS_URL is malformed", e);
            throw new ExceptionInInitializerError("BUG: RESOURCE_CAPS_URL is malformed: " + e.getMessage());
        }
    }

    /**
     * Constructor. Uses a properties file called RegistryClient.properties found in the classpath.
     */
    public RegistryClient()
    {
        this(RESOURCE_CAPS_URL);
    }

    public RegistryClient(URL resourceCapsURL)
    {
        if (resourceCapsURL == null)
        {
            throw new IllegalArgumentException("resourceCapsURL cannot be null");
        }
        init(resourceCapsURL);
    }

    private void init(URL resourceCapsURL)
    {
        try
        {
            String localP = System.getProperty(LOCAL_PROPERTY);
            String hostP = System.getProperty(HOST_PROPERTY);
            String shortHostP = System.getProperty(SHORT_HOST_PROPERTY);
            String domainMatchP = System.getProperty(DOMAIN_MATCH_PROPERTY);

            log.debug("    local: " + localP);
            log.debug("     host: " + hostP);
            log.debug("shortHost: " + shortHostP);
            if ( "true".equals(localP) )
            {
                log.debug(LOCAL_PROPERTY + " is set, assuming localhost runs the service");
                this.hostname = InetAddress.getLocalHost().getCanonicalHostName();
            }

            if (shortHostP != null)
            {
                shortHostP = shortHostP.trim();
                if (shortHostP.length() > 0)
                {
                    this.shortHostname = shortHostP;
                }
            }

            if (hostP != null && this.hostname == null)
            {
                hostP = hostP.trim();
                if (hostP.length() > 0)
                    this.hostname = hostP;
            }

            if (domainMatchP != null)
            {
                String[] doms = domainMatchP.split(",");
                this.domainMatch.addAll(Arrays.asList(doms));
            }

            log.debug("Original resourceCapURL: " + resourceCapsURL);
            this.resourceCapsURL = mangleHostname(resourceCapsURL);
            log.debug("Mangled resourceCapURL: " + this.resourceCapsURL);
            if (!resourceCapsURL.equals(this.resourceCapsURL))
            {
                capsDomain = "alt-domains/" + this.resourceCapsURL.getHost();
            }
        }
        catch(UnknownHostException ex)
        {
            log.warn("failed to find localhost name via name resolution (" + ex.toString() + "): using localhost");
            this.hostname = "localhost";
        }
        catch (MalformedURLException e)
        {
            log.error("Error transforming resource-caps URL", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the capabilities object for the resource identified
     * by resourceID.
     *
     * @param resourceID Identifies the resource.
     * @return The associated capabilities object.
     * @throws IOException If the capabilities could not be determined.
     */
    public Capabilities getCapabilities(URI resourceID) throws IOException
    {
        if (resourceID == null)
        {
            String msg = "Input parameter (resourceID) should not be null";
            throw new IllegalArgumentException(msg);
        }

        File capCacheFile = getCapSourceCacheFile();
        log.debug("Capabilities cache file: " + capCacheFile);
        CachingFile cachedCapSource = new CachingFile(capCacheFile, resourceCapsURL);
        String map = cachedCapSource.getContent();
        InputStream mapStream = new ByteArrayInputStream(map.getBytes("UTF-8"));
        MultiValuedProperties mvp = new MultiValuedProperties();
        try
        {
            mvp.load(mapStream);
        }
        catch (Exception e)
        {
            throw new RuntimeException("failed to load capabilities source map from " + resourceCapsURL, e);
        }

        List<String> values = mvp.getProperty(resourceID.toString());
        if (values == null || values.isEmpty())
        {
            throw new IllegalArgumentException("Unknown service: " + resourceID);
        }
        if (values.size() > 1)
        {
            throw new RuntimeException("Multiple capability locations for " + resourceID);
        }
        URL serviceCapsURL = null;
        try
        {
            serviceCapsURL = new URL(values.get(0));
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("URL for " + resourceID + " at " + resourceCapsURL + " is malformed", e);
        }

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
     * IVOA identifier (e.g. with URI scheme os "ivo").
     *
     * @param resourceIdentifier resource identifier, e.g. ivo://cadc/nrc/ca/tap
     * @param standardID IVOA standard identifier, e.g. ivo://ivo.net/std/TAP#sync-1.1
     * @param authMethod authentication method to be used
     * @return service URL or null if a matching service (and protocol) was not found
     * @throws RuntimeException if more than one URL match the service identifier
     */
    @Deprecated
    public URL getServiceURL(final URI resourceIdentifier, final URI standardID, final AuthMethod authMethod)
    {
        if (resourceIdentifier == null || standardID == null || authMethod == null)
        {
            String msg = "No input parameters should be null";
            throw new IllegalArgumentException(msg);
        }

        URL url = null;
        log.debug("resourceIdentifier=" + resourceIdentifier + ", standardID=" + standardID + ", authMethod=" + authMethod);
        Capabilities caps = null;
        try
        {
            caps = this.getCapabilities(resourceIdentifier);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not obtain service URL", e);
        }

        // locate the associated capability
        Capability cap = caps.findCapability(standardID);

        if (cap != null)
        {
            // locate the associated interface, throws RuntimeException if more than
            // one interface match
            Interface intf = cap.findInterface(Standards.getSecurityMethod(authMethod));

            if (intf != null)
            {
                url = intf.getAccessURL().getURL();
            }
        }

        // return associated access URL, mangle it if necessary
        return url;
    }

    private File getCapSourceCacheFile()
    {
        String cacheBaseDir = getBaseCacheDirectory();
        if (this.capsDomain != null)
        {
            cacheBaseDir += "/" + this.capsDomain;
        }
        String path = "/" + RESOURCE_CAPS_NAME;
        log.debug("Caching file [" + path + "] in dir [" + cacheBaseDir + "]");
        File file = new File(cacheBaseDir + path);
        return file;
    }

    private File getCapabilitiesCacheFile(URI resourceID)
    {
        String baseCacheDir = getBaseCacheDirectory();
        String resourceCacheDir = baseCacheDir + resourceID.getAuthority();
        if (this.capsDomain != null)
        {
            resourceCacheDir = baseCacheDir + this.getCapsDomain() + "/" + resourceID.getAuthority();
        }
        String path = resourceID.getPath();
        log.debug("Caching file [" + path + "] in dir [" + resourceCacheDir + "]");
        File file =  new File(resourceCacheDir, path);
        return file;
    }

    private String getBaseCacheDirectory()
    {
        String userHome = System.getProperty("user.home");
        log.debug("User home: " + userHome);
        if (userHome == null)
        {
            throw new RuntimeException("No home directory");
        }
        return userHome + CONFIG_CACHE_DIR;
    }

    public URL mangleHostname(final URL url) throws MalformedURLException
    {
        URL retURL = url;

        log.debug("mangling URL: " + url);
        if (this.hostname != null || this.shortHostname != null)
        {
            String domain = getDomain(url.getHost());

            if (this.domainMatch.isEmpty() || this.domainMatch.contains(domain))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(url.getProtocol());
                sb.append("://");

                if (this.shortHostname != null)
                {
                    sb.append(this.shortHostname);
                    if (domain != null)
                    {
                        sb.append(".").append(domain);
                    }
                }
                else
                {
                    sb.append(this.hostname);
                }

                int p = url.getPort();

                if (p > 0 && p != url.getDefaultPort())
                {
                    sb.append(":");
                    sb.append(p);
                }

                sb.append(url.getPath());
                retURL = new URL(sb.toString());
            }
        }

        log.debug("mangled URL: " + retURL);
        return retURL;
    }

    public static String getDomain(String hostname)
    {
        if (hostname == null)
        {
            return null;
        }
        int dotIndex = hostname.indexOf('.');
        if (dotIndex <= 0)
        {
            return null;
        }
        if (dotIndex + 1 == hostname.length())
        {
            return null;
        }
        return hostname.substring(dotIndex + 1);
    }

    protected URL getResourceCapsURL()
    {
        return resourceCapsURL;
    }

    protected String getCapsDomain()
    {
        return capsDomain;
    }

}
