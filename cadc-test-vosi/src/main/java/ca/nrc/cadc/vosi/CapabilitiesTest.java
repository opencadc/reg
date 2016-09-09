/*
/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2016.                            (c) 2016.
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
*  $Revision: 4 $
*
************************************************************************
*/

package ca.nrc.cadc.vosi;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.CapabilitiesReader;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Interface;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

/**
 * Tests the capabilities resource of a service. 
 * Note: 
 * 1. The tests require a capabilities file of the service under test
 *    to be in {user.home}/.config/capabilities/{authority}/{service}.
 * 2. The capabilities standard ID (Standards.VOSI_CAPABILITIES_URI) 
 *    is used by the tests.
 * 3. The accessURL of the associated capability should point to the 
 *    server under test, for example your test vm or rc server
 * 
 * @author yeunga
 */
public class CapabilitiesTest
{

    private static final Logger log = Logger.getLogger(CapabilitiesTest.class);
    
    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.vosi", Level.INFO);
    }

    private final URI resourceIdentifier;
    
    public CapabilitiesTest() 
    {
        this(null);
    }
    
    public CapabilitiesTest(URI resourceIdentifier)
    {
        if (resourceIdentifier == null)
        {
            // get resourceIdentifier from system property
            String resourceIdentifierName = CapabilitiesTest.class.getName() + ".resourceIdentifier";
            String resourceIdentifierValue = System.getProperty(resourceIdentifierName);
            log.info(resourceIdentifierName + "=" + resourceIdentifierValue);
            this.resourceIdentifier = URI.create(resourceIdentifierValue);
        }
        else
            this.resourceIdentifier = resourceIdentifier;
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception { }

    @Before
    public void setUp() { }

    @After
    public void tearDown() { }

    private Capabilities getCapabilitiesFromServer(final URL accessURL) 
    		throws IOException, URISyntaxException
    {
        log.info("get capabilties: " + accessURL);
        
        CapabilitiesReader capReader = new CapabilitiesReader();
        InputStream inStream = accessURL.openStream();
		
    	try
    	{
            return capReader.read(inStream);
        }
    	finally
    	{
            if (inStream != null)
            {
                try { inStream.close(); }
                catch(Throwable t)
                {
                    log.warn("failed to close " + accessURL, t);
                }
            }
    	}
    }
    
    /**
     * Optional service-specific validation of content. Override this method to check
     * the content.
     * 
     * @param caps 
     * @throws java.lang.Exception unexpected exception causes test to fail
     */
    protected void validateContent(Capabilities caps)
        throws Exception
    {
        // no-op
    }
    
    // will be removed once RegistryClient.getServiceURL() has been deleted
    @Test
    public void testValidateCapabilitiesUsingGetServiceURL()
    {
    	RegistryClient rc = new RegistryClient();
    	try 
    	{
            URL serviceURL = rc.getServiceURL(resourceIdentifier, Standards.VOSI_CAPABILITIES, AuthMethod.ANON);
            Assert.assertNotNull(serviceURL);
            log.info("serviceURL=" + serviceURL);    	
            
            Capabilities caps = this.getCapabilitiesFromServer(serviceURL);
            validateContent(caps);
	} 
    	catch (Exception t) 
    	{
            log.error("unexpected exception", t);
            Assert.fail("unexpected exception: " + t);
        }
    }
    
    @Test
    public void testValidateCapabilitiesUsingGetCapabilities()
    {
    	RegistryClient rc = new RegistryClient();
    	try 
    	{
            // get the capabilities associated with the resourceIdentifier
            Capabilities caps = rc.getCapabilities(resourceIdentifier);
            Assert.assertNotNull(caps);

            // each web service supports capabilitites, availability and logControl
            // in addition to capabilities specific to the web service
            List<Capability> capList = caps.getCapabilities();
            Assert.assertTrue("Incorrect number of capabilities", capList.size() > 3);

            // get the capability associated with the standard ID
            Capability cap = caps.findCapability(Standards.VOSI_CAPABILITIES);
            Assert.assertNotNull(cap);

            // get the interface associated with the securityMethod
            Interface intf = cap.findInterface(Standards.getSecurityMethod(AuthMethod.ANON));
            Assert.assertNotNull(intf);

            validateContent(caps);
        } 
    	catch (Exception t) 
    	{
            log.error("unexpected exception", t);
            Assert.fail("unexpected exception: " + t);
        }
    }
}
