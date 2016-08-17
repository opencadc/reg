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
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Standards;
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
        Log4jInit.setLevel("ca.nrc.cadc", Level.INFO);
    }

    //static String GMS_URI = "ivo://cadc.nrc.ca/gms";
    //static String GMS_HTTP = "http://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/gms";
    //static String GMS_HTTPS = "https://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/gms";

    //static String VOS_URI = "ivo://cadc.nrc.ca/vospace";
    //static String VOS_HTTP = "http://www.canfar.phys.uvic.ca/vospace";
    //static String VOS_HTTPS = "https://www.canfar.phys.uvic.ca/vospace";

    static String STANDARD_ID = "ivo://ivoa.net/std/TAP#sync-1.1";
    static String RESOURCE_ID = "ivo://cadc.nrc.ca/tap";
    static String RESOURCE_ID_NO_VALUE = "ivo://cadc.nrc.ca/novalue";
    static String RESOURCE_ID_NO_AUTH_METHOD = "ivo://cadc.nrc.ca/noauthmethod";
    static String RESOURCE_ID_NOT_FOUND = "ivo://cadc.nrc.ca/notfound";

    @BeforeClass
    public static void touchConfigFile()
    {
        File config = new File("test/.config/cadcRegistry/cadc.nrc.ca/tap");
        config.setLastModified(System.currentTimeMillis());
        log.debug("Touched file: " + config);
    }

    @Test
    public void testGetCapabilitiesWithNullResourceidentifier()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	RegistryClient rc = new RegistryClient();
    	try
    	{
			rc.getCapabilities(null);
            Assert.fail("expected IllegalArgumentException");
		}
    	catch (IllegalArgumentException ex)
    	{
    		// expecting null input parameter message
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
        finally
        {
            // restore user.home environment
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetCapabilitiesMissingPropertyValue()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	RegistryClient rc = new RegistryClient();
    	try
    	{
			rc.getCapabilities(new URI(RESOURCE_ID_NO_VALUE));
            Assert.fail("expected RuntimeException");
		}
    	catch (RuntimeException ex)
    	{
    		// expecting not able to find the cache resource
    		if (!ex.getMessage().toLowerCase().contains("unknown service"))
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
        finally
        {
            // restore user.home environment
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetCapabilitiesHappyPath()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	RegistryClient rc = new RegistryClient();
    	try
    	{
			Capabilities caps = rc.getCapabilities(new URI(RESOURCE_ID));
			List<Capability> capList = caps.getCapabilities();
			Assert.assertTrue("Incorrect number of capabilities", capList.size() > 3);
		}
    	catch (Throwable t)
    	{
            log.error("unexpected exception", t);
    		Assert.fail("unexpected exception: " + t);
		}
        finally
        {
            // restore user.home environment
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetServiceURLWithNullAuthMethod()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		URI resourceID = new URI("ivo://cadc.nrc.ca/tap");
    		URI standardID = Standards.TAP_SYNC_11;
    		AuthMethod authMethod = null;
    		rc.getServiceURL(resourceID, standardID, authMethod);
    	}
    	catch (IllegalArgumentException ex)
    	{
    		// expecting a null parameter related exception
    		if (!ex.getMessage().contains("No input parameters should be null"))
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
        finally
        {
            // restore user.home environment
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetServiceURLWithNullStandardID()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

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
    		if (!ex.getMessage().contains("No input parameters should be null"))
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
        finally
        {
            // restore user.home environment
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetServiceURLWithNullResourceID()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		URI resourceID = null;
    		URI standardID = Standards.TAP_SYNC_11;
    		AuthMethod authMethod = AuthMethod.getAuthMethod("anon");
    		rc.getServiceURL(resourceID, standardID, authMethod);
    	}
    	catch (IllegalArgumentException ex)
    	{
    		// expecting a null parameter related exception
    		if (!ex.getMessage().contains("No input parameters should be null"))
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
        finally
        {
            // restore user.home environment
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetServiceURLHappyPath()
    {
    	// save user.home environment
    	String currentUserHome = System.getProperty("user.home");
    	System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	RegistryClient rc = new RegistryClient();
    	try
    	{
    		URL expected = new URL("https://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/tap/sync");
    		URI resourceID = new URI(RESOURCE_ID);
    		URI standardID = new URI(STANDARD_ID);
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(resourceID, standardID, authMethod);
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
    		// restore user.home environment
    		System.setProperty("user.home", currentUserHome);
    	}
    }

    @Test
    public void testGetServiceURLModifyLocal()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".local", "true");
            RegistryClient rc = new RegistryClient();
            String localhost = InetAddress.getLocalHost().getCanonicalHostName();
            URL expected = new URL("https://" + localhost + "/tap/sync");

    		URI resourceID = new URI(RESOURCE_ID);
    		URI standardID = new URI(STANDARD_ID);
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(resourceID, standardID, authMethod);
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
            System.setProperty("user.home", currentUserHome);
        }
    }

    //@Test
    public void testGetServiceURLModifyHost()
    {
        String currentUserHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".host", "foo.bar.com");
            RegistryClient rc = new RegistryClient();
            String expected = "https://foo.bar.com/tap/sync";

    		URI resourceID = new URI(RESOURCE_ID);
    		URI standardID = new URI(STANDARD_ID);
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(resourceID, standardID, authMethod);
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
            System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetServiceURLModifyShortHostname()
    {
    	// save user.home environment
    	String currentUserHome = System.getProperty("user.home");
    	System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "foo");
            RegistryClient rc = new RegistryClient();
            String expected = "https://foo.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/tap/sync";

    		URI resourceID = new URI(RESOURCE_ID);
    		URI standardID = new URI(STANDARD_ID);
    		AuthMethod authMethod = AuthMethod.getAuthMethod("cert");
    		URL serviceURL = rc.getServiceURL(resourceID, standardID, authMethod);
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
    		System.setProperty("user.home", currentUserHome);
        }
    }

    @Test
    public void testGetServiceURLMatchDomain()
    {
    	// save user.home environment
    	String currentUserHome = System.getProperty("user.home");
    	System.setProperty("user.home", System.getProperty("user.dir") + "/test");

    	try
    	{
            System.setProperty(RegistryClient.class.getName() + ".shortHostname", "foo");
            System.setProperty(RegistryClient.class.getName() + ".domainMatch", "cadc-ccda.hia-iha.nrc-cnrc.gc.ca,other.com");
            RegistryClient rc = new RegistryClient();
            String expected1 = "https://foo.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/tap/sync";

    		URI resourceID1 = new URI(RESOURCE_ID);
    		URI standardID1 = new URI(STANDARD_ID);
    		AuthMethod authMethod1 = AuthMethod.getAuthMethod("cert");
    		URL serviceURL1 = rc.getServiceURL(resourceID1, standardID1, authMethod1);
    		Assert.assertNotNull("Service URL should not be null", serviceURL1);
    		Assert.assertEquals("got an incorrect URL", expected1, serviceURL1.toExternalForm());
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
    		System.setProperty("user.home", currentUserHome);
        }
    }
}
