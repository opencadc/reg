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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.StringUtil;
import ca.nrc.cadc.vosi.Capabilities;
import ca.nrc.cadc.vosi.Capability;
import ca.nrc.cadc.vosi.Interface;


/**
 * A very simple caching IVOA Registry client. All the lookups done by this client use a properties
 * file named RegistryClient.properties found via the classpath.
 * </p><p>
 * Note for developers: You can set a system property to force this class to replace the hostname
 * in the resuting URL with the canonical hostname of the local host. This is useful for testing:
 * </p>
 * <pre>
 * ca.nrc.cadc.reg.client.RegistryClient.local=true
 * </pre>
 * </p><p>
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

    private static final String CACHE_FILENAME = RegistryClient.class.getSimpleName() + ".properties";
    private static final String LOCAL_PROPERTY = RegistryClient.class.getName() + ".local";
    private static final String HOST_PROPERTY = RegistryClient.class.getName() + ".host";
    private static final String SHORT_HOST_PROPERTY = RegistryClient.class.getName() + ".shortHostname";
    private static final String DOMAIN_MATCH_PROPERTY = RegistryClient.class.getName() + ".domainMatch";

    private URL url;
    private MultiValuedProperties mvp;

    private String hostname;
    private String shortHostname;
    private List<String>domainMatch = new ArrayList<String>();

    /**
     * Constructor. Uses a properties file called RegistryClient.properties found in the classpath.
     */
    public RegistryClient()
    {
        try
        {
            File conf = new File(System.getProperty("user.home") + "/config", CACHE_FILENAME);
            URL furl;
            if (conf.exists())
                furl = new URL("file://" + conf.getAbsolutePath());
            else
                furl = RegistryClient.class.getResource("/"+CACHE_FILENAME);

            init(furl, false);
        }
        catch(Exception ex)
        {
            throw new RuntimeException("failed to find URL to " + CACHE_FILENAME, ex);
        }
    }

    /**
     * Constructor. Uses a properties file loaded from the specified url.
     * @param url
     */
    public RegistryClient(URL url)
    {
        init(url, false);
    }
    
    /**
     * Find the capabilities for the service specified by its resource identifier. The 
     * specified resource identifier must be an IVOA identifier (e.g. with URI 
     * scheme of "ivo" like ivo://cadc.nrc.ca/vospace). 
     *
     * @param resourceID base resource identifier of the service
     * @return service capabilities
     * @throws URISyntaxException 
     */
    public Capabilities getCapabilities(final URI resourceID) 
    		throws URISyntaxException
    {
        init();
        log.debug("getCapabilities: " + resourceID);

        Capabilities caps = new Capabilities(resourceID);
        Set<String> keySet = this.mvp.keySet();
        if (keySet == null || keySet.isEmpty())
        {
        	// no way to look up resource identifier
        	return caps;
        }
        
        // go through all configured capabilities and 
        // find the ones that match the resourceID
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext())
        {
        	// key = <resource identifier>#<feature>
        	String key = iter.next();
        	String[] keyParts = key.split("#");        	
        	if (keyParts[0].equals(resourceID.toString()))
        	{
        		// this is the capability we are looking for, add it.
        		List<String> values = this.mvp.getProperty(key);
        		if (values == null || values.isEmpty())
        		{
        			String msg = "Missing property value for property " + key;
        			throw new IllegalArgumentException(msg);
        		}
        		
        		// add interfaces to a capability
        		Capability cap = new Capability(this.getStandardID(key));
        		for (String value : values)
        		{
        			if (value.length() == 0)
        			{
            			String msg = "Missing a property value for property " + key;
            			throw new IllegalArgumentException(msg);
        			}
        			
        			// get an interface
        			String[] valueParts = value.split(" "); 
        			if (valueParts.length < 2)
        			{
        				String msg = "Missing either access URL or authentication method in properties file.";
        				throw new IllegalArgumentException(msg);
        			}
        			
        			String accessString = valueParts[0];
        			Interface interf = new Interface(new URI(accessString));
        			String[] ams = valueParts[1].split(",");
        			for (String am : ams)
        			{
                        AuthMethod authMethod = AuthMethod.getAuthMethod(am);
                        interf.getSecurityMethods().add(authMethod.getSecurityMethod());
        			}
        			
        			// add the interface
        			cap.getInterfaces().add(interf);
        		};
        		
        		// add the capability
        		caps.getCapabilities().add(cap);
        	}
        }
        
        return caps;
    }

    /**
     * Find the service URL for the service registered under the specified base resource 
     * identifier and using the specified authentication method. The identifier must be an 
     * IVOA identifier (e.g. with URI scheme os "ivo"). 
     *
     * @param serviceID service identifier consisting of <resource identifier>#<feature>
     * @param authMethod authentication method to be used
     * @return service URL or null if a matching service (and protocol) was not found
     * @throws RuntimeException if more than one URL match the service identifier
     * @throws URISyntaxException 
     * @throws MalformedURLException 
     */
    @Deprecated
    public URL getServiceURL(final URI serviceID, AuthMethod authMethod) 
    		throws URISyntaxException, MalformedURLException
    {
    	if (serviceID == null || authMethod == null)
    	{
    		String msg = "Both service identifier and authentication method should not be null";
    		throw new IllegalArgumentException(msg);
    	}
    	
    	URL url = null;
    	
    	// extract the resource identifier and get the associated capabilities
    	String[] parts = serviceID.toString().split("#");
    	Capabilities caps = this.getCapabilities(new URI(parts[0]));
    	
    	// locate the associated capability
    	Capability cap = caps.findCapability(this.getStandardID(serviceID.toString()));
    	
    	if (cap != null)
    	{
    	    // locate the associated interface, throws RuntimeException if more than
    	    // one interface match
    	    Interface intf = cap.findInterface(authMethod.getSecurityMethod());
    	    if (intf != null)
    	    {
    	        URL intfURL = intf.getAccessURL().toURL();
    	        url = mangleHostname(intfURL);
    	    }
    	}
    	
    	// return associated access URL, mangle it if necessary
        return url;
    }

    /**
     * Find the service URL for the service registered under the specified base resource 
     * identifier and using the specified authentication method. The identifier must be an 
     * IVOA identifier (e.g. with URI scheme os "ivo"). 
     *
     * @param serviceID service identifier consisting of <resource identifier>#<feature>
     * @param authMethod authentication method to be used
     * @param path the path to be appended to the service URL prior to returning it
     * @return base URL or null if a matching service (and protocol) was not found
     * @throws URISyntaxException 
     * @throws MalformedURLException 
     */
    @Deprecated
    public URL getServiceURL(URI serviceID, AuthMethod authMethod, String path) 
    		throws URISyntaxException, MalformedURLException
    {
    	URL serviceURL = this.getServiceURL(serviceID, authMethod);
    	
        if (serviceURL != null && StringUtil.hasText(path))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(serviceURL.toString());
            
            if ( !path.startsWith("/") && !path.startsWith("?") )
            {
                sb.append("/");
            }
            
            sb.append(path);
            serviceURL = new URL(sb.toString());
        }
    	
        return serviceURL;
    }

    private void init()
    {
        if (mvp != null)
            return;

        InputStream istream = null;
        try
        {
            // find the cache resource from the url
            if (url == null)
                throw new RuntimeException("failed to find cache resource.");

            // read the properties
            log.debug("init: reading config from " + url);
            istream = url.openStream();
            this.mvp = new MultiValuedProperties();
            mvp.load(istream);

            if (log.isDebugEnabled())
            {
                for (String k : mvp.keySet())
                {
                    List<String> values = mvp.getProperty(k);
                    for (String v : values)
                    {
                        log.debug(k + " = " + v);
                    }
                }
            }
        }
        catch(IOException ex)
        {
            throw new RuntimeException("failed to load resource: " + CACHE_FILENAME, ex);
        }
        finally
        {
            if (istream != null)
                try { istream.close(); }
                catch(Throwable t)
                {
                    log.warn("failed to close " + url, t);
                }
        }
    }

    private void init(URL url, boolean unused)
    {
        this.url = url;
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
        }
        catch(UnknownHostException ex)
        {
            log.warn("failed to find localhost name via name resolution (" + ex.toString() + "): using localhost");
            this.hostname = "localhost";
        }
    }
    
    private URI getStandardID(final String serviceID) throws URISyntaxException
    {
    	// TODO: implement once we have decided how to associate 
    	//       a resource identifier to a standard identifier
    	return new URI(serviceID);
    }

    private boolean shouldMangleHostname(final URL url)
    {
        boolean mangleHostname = false;
        String domain = getDomain(url.getHost());
        if (this.hostname != null || this.shortHostname != null)
        {
            //domainMatch.isEmpty : all
            if (this.domainMatch.isEmpty() || this.domainMatch.contains(domain))
            {
                mangleHostname = true;
            }
        }
        
        return mangleHostname;
    }
    
    private URL mangleHostname(final URL url) throws MalformedURLException
    {
    	URL retURL = url;
    	
        if (shouldMangleHostname(url))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(url.getProtocol());
            sb.append("://");
            
            if (this.shortHostname != null)
            {
                sb.append(this.shortHostname);
                String domain = getDomain(url.getHost());
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
}
