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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockserver.model.HttpRequest.request;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.model.ClearType;
import org.mockserver.verify.VerificationTimes;

import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;

/**
 * A set of Junit tests that use a MockServer to test a how the RegistryClient handles good responses for service capabilities. 
 * See https://www.mock-server.com/
 * 
 */
public class MockServerGoodCapabilitiesTests
extends MockServerTestBase
    {
    private static final Logger log = Logger.getLogger(MockServerGoodCapabilitiesTests.class);

    @Before
    @Override
    public void setupMockServer()
    throws IOException {

        super.setupMockServer();
        
    }
    
    @Test
    public void testSingleServiceCapabilities()
        throws Exception {

        //
        // A single good registry.
        RegistryClient registryClient = buildRegistryClient(
            "http://testhost-001:1080/good-registry-001"
            );
        
        //
        // Get the good service capabilities.
        try {
            Capabilities capabilities = registryClient.getCapabilities(new URI("ivo://good.authority/good-service"));
            assertNotNull(capabilities);
            List<Capability> list = capabilities.getCapabilities();
            assertTrue(
                list.size() == 2
                );
        }
        catch (Exception ouch) {
            log.warn(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            fail(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }

        //
        // The registry client should call the 'resource-caps' endpoint once to get the list of capabilities endpoints.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-001/resource-caps"
                    ),
                VerificationTimes.exactly(1)
        );        

        //
        // The registry client should call the 'good-capabilities' endpoint once to get the service capabilities.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(1)
            );        

        //
        // Clear the MockServer request logs.
        mockServer.clear(
            request(),
            ClearType.LOG
            );
        
        //
        // Get the good service capabilities again.
        try {
            Capabilities capabilities = registryClient.getCapabilities(new URI("ivo://good.authority/good-service"));
            assertNotNull(capabilities);
            List<Capability> list = capabilities.getCapabilities();
            assertTrue(
                list.size() == 2
                );
        }
        catch (Exception ouch) {
            log.warn(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            fail(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }

        //
        // The registry client should have a cached version of the capabilities and should not not have called the 'resource-caps' endpoint again.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-001/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        

        //
        // The registry client should have a cached version of good service capabilities and should not not have called the 'good-capabilities' endpoint again.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(0)
            );        
    }         
    
    @Test
    public void testMultipleServiceCapabilities()
        throws Exception {

        //
        // Three good registries.
        RegistryClient registryClient = buildRegistryClient(
                "http://testhost-001:1080/good-registry-001",
                "http://testhost-002:1080/good-registry-001",
                "http://testhost-003:1080/good-registry-001"
            );
        
        //
        // Get the good service capabilities.
        try {
            Capabilities capabilities = registryClient.getCapabilities(new URI("ivo://good.authority/good-service"));
            assertNotNull(capabilities);
            List<Capability> list = capabilities.getCapabilities();
            assertTrue(
                list.size() == 2
                );
        }
        catch (Exception ouch) {
            log.warn(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            fail(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }

        //
        // The registry client should call the 'resource-caps' endpoint once to get the list of capabilities endpoints.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-001/resource-caps"
                    ),
                VerificationTimes.exactly(1)
        );        

        //
        // The registry client should not have called the other registries.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-002/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-003/resource-caps"
                    ),
                VerificationTimes.exactly(0)
            );        

        
        //
        // The registry client should call the 'good-capabilities' endpoint once to get the service capabilities.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(1)
            );        

        //
        // Clear the MockServer request logs.
        mockServer.clear(
            request(),
            ClearType.LOG
            );
        
        //
        // Get the good service capabilities again.
        try {
            Capabilities capabilities = registryClient.getCapabilities(new URI("ivo://good.authority/good-service"));
            assertNotNull(capabilities);
            List<Capability> list = capabilities.getCapabilities();
            assertTrue(
                list.size() == 2
                );
        }
        catch (Exception ouch) {
            log.warn(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            fail(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }

        //
        // The registry client should have a cached version of the capabilities and should not not have called the 'resource-caps' endpoint again.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-001/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        

        //
        // The registry client should not have called the other registries.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-002/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-003/resource-caps"
                    ),
                VerificationTimes.exactly(0)
            );        
        
        //
        // The registry client should have a cached version of good service capabilities and should not not have called the 'good-capabilities' endpoint again.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(0)
            );        
    }         
}


