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

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Standard;
import ca.nrc.cadc.util.Log4jInit;

/**
 *
 * @author pdowler
 */
public class RegistryClientTest
{
private static Logger log = Logger.getLogger(RegistryClientTest.class);
    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
    }


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }

    //static String GMS_URI = "ivo://cadc.nrc.ca/gms";
    //static String GMS_HTTP = "http://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/gms";
    //static String GMS_HTTPS = "https://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/gms";

    //static String VOS_URI = "ivo://cadc.nrc.ca/vospace";
    //static String VOS_HTTP = "http://www.canfar.phys.uvic.ca/vospace";
    //static String VOS_HTTPS = "https://www.canfar.phys.uvic.ca/vospace";

    static String DUMMY_URI = "ivo://example.com/srv";
    static String OTHER_URI = "ivo://example.com/bar";
    static String LONG_URI = "ivo://example.com/long";
    static String DUMMY_URL = "http://www.example.com/current/path/to/my/service";
    static String DUMMY_SURL = "https://www.example.com/current/path/to/my/service";
    static String DUMMY_CERT_URL = "https://www.example.com/current/path/to/my/x509-service";
    static String DUMMY_PASSWORD_URL = "http://www.example.com/current/path/to/my/auth-service";
    static String DUMMY_TOKEN_URL = DUMMY_URL;
    static String DUMMY_COOKIE_URL = DUMMY_URL;
    
    static String RESOURCE_ID = "ivo://cadc.nrc.ca/tap";
    static String RESOURCE_ID_NO_VALUE = "ivo://cadc.nrc.ca/novalue";
    static String RESOURCE_ID_NO_AUTH_METHOD = "ivo://cadc.nrc.ca/noauthmethod";
    static String RESOURCE_ID_NOT_FOUND = "ivo://cadc.nrc.ca/notfound";

    @Test
    public void testGetCapabilitiesMissingPropertyValue()
    {
    	RegistryClient rc = new RegistryClient();
    	try 
    	{
			Capabilities caps = rc.getCapabilities(new URI(RESOURCE_ID_NO_VALUE));
			Assert.assertEquals("resource identifier is different", RESOURCE_ID_NO_VALUE, caps.getResourceIdentifier().toString());
			caps.getCapabilities();
            Assert.fail("expected IllegalArgumentException");			
		} 
    	catch (IllegalArgumentException ex)
    	{
    		// expecting a property value related exception
    		if (!ex.getMessage().contains("property value"))
    		{
                log.error("unexpected exception", ex);
        		Assert.fail("unexpected exception: " + ex);
    		}
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }

    @Test
    public void testGetCapabilitiesMissingAuthMethod()
    {
    	RegistryClient rc = new RegistryClient();
    	try 
    	{
			Capabilities caps = rc.getCapabilities(new URI(RESOURCE_ID_NO_AUTH_METHOD));
			Assert.assertEquals("resource identifier is different", RESOURCE_ID_NO_AUTH_METHOD, caps.getResourceIdentifier().toString());
			List<Capability> capList = caps.getCapabilities();
            Assert.fail("expected IllegalArgumentException");
		} 
    	catch (IllegalArgumentException ex)
    	{
    		// expecting an auth method related exception
    		if (!ex.getMessage().contains("authentication method"))
    		{
                log.error("unexpected exception", ex);
        		Assert.fail("unexpected exception: " + ex);
    		}
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }

    @Test
    public void testGetCapabilitiesNotFound()
    {
    	RegistryClient rc = new RegistryClient();
    	try 
    	{
			Capabilities caps = rc.getCapabilities(new URI(RESOURCE_ID_NOT_FOUND));
			Assert.assertEquals("resource identifier is different", RESOURCE_ID_NOT_FOUND, caps.getResourceIdentifier().toString());
			List<Capability> capList = caps.getCapabilities();
			Assert.assertEquals("Incorrect number of capabilities", 0, capList.size());
		} 
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }

    @Test
    public void testGetCapabilitiesHappyPath()
    {
    	RegistryClient rc = new RegistryClient();
    	try 
    	{
			Capabilities caps = rc.getCapabilities(new URI(RESOURCE_ID));
			Assert.assertEquals("resource identifier is different", RESOURCE_ID, caps.getResourceIdentifier().toString());
			List<Capability> capList = caps.getCapabilities();
			Assert.assertEquals("Incorrect number of capabilities", 3, capList.size());
		} 
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }
    
    @Test
    public void testGetServiceURLWithNullAuthMethod()
    {
    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		URI resourceID = new URI("ivo://cadc.nrc.ca/tap");
    		URI standardID = new URI(Standard.TAP_SYNC_1_1);
    		AuthMethod authMethod = null;
    		rc.getServiceURL(resourceID, standardID, authMethod);
    	}
    	catch (IllegalArgumentException ex)
    	{
    		// expecting a null parameter related exception
    		if (!ex.getMessage().contains("should not be null"))
    		{
                log.error("unexpected exception", ex);
        		Assert.fail("unexpected exception: " + ex);
    		}
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }
    
    @Test
    public void testGetServiceURLWithNullStandardID()
    {
    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		URI resourceID = new URI("ivo://cadc.nrc.ca/tap");
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("anon");
    		rc.getServiceURL(resourceID, standardID, authMethod);
    	}
    	catch (IllegalArgumentException ex)
    	{
    		// expecting a null parameter related exception
    		if (!ex.getMessage().contains("should not be null"))
    		{
                log.error("unexpected exception", ex);
        		Assert.fail("unexpected exception: " + ex);
    		}
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }
    
    @Test
    public void testGetServiceURLWithNullResourceID()
    {
    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		URI resourceID = null;
    		URI standardID = new URI(Standard.TAP_SYNC_1_1);
    		AuthMethod authMethod = AuthMethod.getAuthMethod("anon");
    		rc.getServiceURL(resourceID, standardID, authMethod);
    	}
    	catch (IllegalArgumentException ex)
    	{
    		// expecting a null parameter related exception
    		if (!ex.getMessage().contains("should not be null"))
    		{
                log.error("unexpected exception", ex);
        		Assert.fail("unexpected exception: " + ex);
    		}
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }
    
    @Test
    public void testGetServiceURLHappyPath()
    {
    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		// TODO: fix test parameters
    		URL expected = new URL("https://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/cred");
    		URI serviceID = new URI("ivo://cadc.nrc.ca/cred#delegate");
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(serviceID, standardID, authMethod);
    		Assert.assertNotNull("Service URL should not be null", serviceURL);
    		Assert.assertEquals("got an incorrect URL", expected, serviceURL);
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
    }
    
    @Test
    public void testGetServiceURLViaConfigFile()
    {
        String home = System.getProperty("user.home");
    	try
    	{
            String fakeHome = System.getProperty("user.dir") + "/test";
            log.debug("setting user.home = " + fakeHome);
            System.setProperty("user.home", fakeHome);
            RegistryClient rc = new RegistryClient();

            // TODO: fix test parameters
            URL expected = new URL("http://alt.example.com/current/path/to/my/service");
    		URI serviceID = new URI("ivo://example.com/srv");
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cookie");
    		URL serviceURL = rc.getServiceURL(serviceID, standardID, authMethod);
    		Assert.assertNotNull("Service URL should not be null", serviceURL);
    		Assert.assertEquals("got an incorrect URL", expected, serviceURL);
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // reset
            System.setProperty("user.home", home);
        }
    }
    
    @Test
    public void testGetServiceURLModifyLocal()
    {
    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".local", "true");
            RegistryClient rc = new RegistryClient();
            String localhost = InetAddress.getLocalHost().getCanonicalHostName();
            URL expected = new URL("https://" + localhost + "/cred");

            // TODO: fix test parameters
    		URI serviceID = new URI("ivo://cadc.nrc.ca/cred#delegate");
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(serviceID, standardID, authMethod);
    		Assert.assertNotNull("Service URL should not be null", serviceURL);
    		Assert.assertEquals("got an incorrect URL", expected, serviceURL);
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // reset
            System.setProperty(RegistryClient.class.getName() + ".local", "false");
        }
    }
    
    @Test
    public void testGetServiceURLModifyHost()
    {
    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".host", "foo.bar.com");
            RegistryClient rc = new RegistryClient();
            String expected = "https://foo.bar.com/cred";

            // TODO: fix  test parameters
    		URI serviceID = new URI("ivo://cadc.nrc.ca/cred#delegate");
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(serviceID, standardID, authMethod);
    		Assert.assertNotNull("Service URL should not be null", serviceURL);
    		Assert.assertEquals("got an incorrect URL", expected, serviceURL.toExternalForm());
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // reset
            System.setProperty(RegistryClient.class.getName() + ".host", "");
        }
    }
    
    @Test
    public void testGetServiceURLModifyShortHostname()
    {
    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "foo");
            RegistryClient rc = new RegistryClient();
            String expected = "https://foo.example.com/current/path/to/my/x509-service";

            // TODO: fix test parameters
    		URI serviceID = new URI(DUMMY_URI);
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(serviceID, standardID, authMethod);
    		Assert.assertNotNull("Service URL should not be null", serviceURL);
    		Assert.assertEquals("got an incorrect URL", expected, serviceURL.toExternalForm());
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // reset
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "");
        }
    }
    
    @Test
    public void testGetServiceURLModifyShortHostnameLongDomain()
    {
    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "foo");
            RegistryClient rc = new RegistryClient();
            String expected = "https://foo.long.domain.example.net/current/path/to/my/x509-service";

            // TODO: fix test parameters
    		URI serviceID = new URI(LONG_URI);
    		URI standardID = null;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(serviceID, standardID, authMethod);
            log.info("long url: " + serviceURL);
    		Assert.assertNotNull("Service URL should not be null", serviceURL);
    		Assert.assertEquals("got an incorrect URL", expected, serviceURL.toExternalForm());
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // reset
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "");
        }
    }
  
    @Test
    public void testGetServiceURLMatchDomain()
    {
    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "foo");
            System.setProperty(RegistryClient.class.getName() + ".domainMatch", "example.com,other.com");
            RegistryClient rc = new RegistryClient();
            String expected1 = "https://foo.example.com/current/path/to/my/x509-service";

            // TODO: fix test parameters
    		URI serviceID1 = new URI(DUMMY_URI);
    		URI standardID1 = null;
    		AuthMethod authMethod1 = AuthMethod.getAuthMethod("cert");
    		URL serviceURL1 = rc.getServiceURL(serviceID1, standardID1, authMethod1);
    		Assert.assertNotNull("Service URL should not be null", serviceURL1);
    		Assert.assertEquals("got an incorrect URL", expected1, serviceURL1.toExternalForm());
    		
    		// TODO: fix test parameters
            String expected2 = "https://www.example.net/current/path/to/my/x509-service";
     		URI serviceID2 = new URI(OTHER_URI);
     		URI standardID2 = null;
    		AuthMethod authMethod2 = AuthMethod.getAuthMethod("cert");
    		URL serviceURL2 = rc.getServiceURL(serviceID2, standardID2, authMethod2);
    		Assert.assertNotNull("Service URL should not be null", serviceURL2);
    		Assert.assertEquals("got an incorrect URL", expected2, serviceURL2.toExternalForm());
    		
    	}
    	catch (Throwable t) 
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // reset
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "");
            System.setProperty(RegistryClient.class.getName() + ".domainMatch", "");
        }
    }
}
