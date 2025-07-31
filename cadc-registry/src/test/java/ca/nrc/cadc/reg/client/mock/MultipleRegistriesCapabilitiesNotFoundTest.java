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

package ca.nrc.cadc.reg.client.mock;

import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.ClearType;
import org.mockserver.verify.VerificationTimes;

import ca.nrc.cadc.net.ResourceNotFoundException;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;

/**
 * A set of Junit tests that use a MockServer to test a how the RegistryClient handles a missing capabilities response. 
 * See https://www.mock-server.com/
 * 
 */
public class MultipleRegistriesCapabilitiesNotFoundTest
    {
    private static final Logger log = Logger.getLogger(MultipleRegistriesCapabilitiesNotFoundTest.class);
    static {
    Log4jInit.setLevel("ca.nrc.cadc.reg", Level.DEBUG);
    Log4jInit.setLevel("ca.nrc.cadc.net", Level.DEBUG);
    }

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void startMockServer() {
        mockServer = startClientAndServer(1080);
    }
    
    @AfterClass
    public static void stopMockServer() {
        mockServer.stop();
    }

    @Before
    public void setupMockServer() {
        
        //
        // Reset the MockServer
        mockServer.reset();
    
        //
        // Setup the good 'resource-caps' responses.
        mockServer.when(
            request()
                .withPath(
                    "/good-registry-one/resource-caps"
                    )
                )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody(
                        "ivo://good.authority/good-service = http://localhost:1080/good-service/missing-capabilities"
                        )
        );
        mockServer.when(
                request()
                    .withPath(
                        "/good-registry-two/resource-caps"
                        )
                    )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withBody(
                            "ivo://good.authority/good-service = http://localhost:1080/good-service/missing-capabilities"
                            )
            );
        mockServer.when(
                request()
                    .withPath(
                        "/good-registry-three/resource-caps"
                        )
                    )
                .respond(
                    response()
                        .withStatusCode(200)
                        .withBody(
                            "ivo://good.authority/good-service = http://localhost:1080/good-service/missing-capabilities"
                            )
            );

        //
        // Setup the 'missing-capabilities' response to return a 404 error code.
        mockServer.when(
            request()
                .withPath(
                    "/good-service/missing-capabilities"
                    )
                )
            .respond(
                response()
                    .withStatusCode(
                        HttpStatus.SC_NOT_FOUND
                        )
                    .withBody(
                        "These are not the capabilities you are looking for"
                        )
                    );

    }

    private RegistryClient registryClient ;

    @Before
    public void setupRegClient()
        throws IOException {
    
        //
        // Create the registry client configuration file.
        File configFile = File.createTempFile("good-config", "properties");
        PrintWriter printWriter = new PrintWriter(
            new FileWriter(configFile)
            );
        printWriter.print(
                "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/good-registry-one"
              + "\n"
              + "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/good-registry-two"
              + "\n"
              + "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/good-registry-three"
            );
        printWriter.close();
        
        //
        // Create the registry client and clear the cache directory.
        registryClient = new RegistryClient(configFile);
        registryClient.deleteCache();

    }
    
    @Test
    public void testCapabilitiesNotFoundOnce()
        throws Exception {
        
        //
        // Try to get the service capabilities.
        try {
            Capabilities capabilities = registryClient.getCapabilities(
                new URI("ivo://good.authority/good-service")
                );
            List<Capability> list = capabilities.getCapabilities();
            fail(
                "Should not have reached this point"
                );
        }
        catch (IOException ouch) {
            log.debug(
                "Expected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            if (ouch.getCause() instanceof ResourceNotFoundException)
                {
                log.debug(
                    "Expected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                    );
                }
            else {
                log.warn(
                    "Unexpected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                    );
                fail(
                    "Unexpected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                );
            }
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
        // The registry client should have called the 'resource-caps' endpoint on the first registry once to get the list of capabilities endpoints.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-one/resource-caps"
                    ),
                VerificationTimes.exactly(1)
        );        
        //
        // The registry client should not have called the other registries.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-two/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-three/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        

        //
        // The registry client should have called the 'missing-capabilities' endpoint twice, once to try to populate the cache,
        // and a second time to try to get the content without using the cache.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/missing-capabilities"
                    ),
                VerificationTimes.exactly(2)
            );        
    }         

    @Test
    public void testCapabilitiesNotFoundTwice()
        throws Exception {

        //
        // Try to get the service capabilities.
        try {
            Capabilities capabilities = registryClient.getCapabilities(
                new URI("ivo://good.authority/good-service")
                );
            List<Capability> list = capabilities.getCapabilities();
            fail(
                "Should not have reached this point"
                );
        }
        catch (IOException ouch) {
            log.debug(
                "Expected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            if (ouch.getCause() instanceof ResourceNotFoundException)
                {
                log.debug(
                    "Expected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                    );
                }
            else {
                log.warn(
                    "Unexpected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                    );
                fail(
                    "Unexpected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                );
            
            }
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
        // The registry client should have called the 'resource-caps' endpoint on the first registry once to get the list of capabilities endpoints.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-one/resource-caps"
                    ),
                VerificationTimes.exactly(1)
        );        
        //
        // The registry client should not have called the other registries.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-two/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-three/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        

        //
        // The registry client should have called the 'missing-capabilities' endpoint twice, once to try to populate the cache,
        // and a second time to try to get the content without using the cache.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/missing-capabilities"
                    ),
                VerificationTimes.exactly(2)
            );        
 
        //
        // Clear the MockServer request logs.
        mockServer.clear(
            request(),
            ClearType.LOG
            );
        
        //
        // Try to get the service capabilities.
        try {
            Capabilities capabilities = registryClient.getCapabilities(
                new URI("ivo://good.authority/good-service")
                );
            List<Capability> list = capabilities.getCapabilities();
            fail(
                "Should not have reached this point"
                );
        }
        catch (IOException ouch) {
            log.debug(
                "Expected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
            if (ouch.getCause() instanceof ResourceNotFoundException)
                {
                log.debug(
                    "Expected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                    );
                }
            else {
                log.warn(
                    "Unexpected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                    );
                fail(
                    "Unexpected cause [" + ouch.getCause().getClass().getSimpleName() + "][" + ouch.getCause().getMessage() + "]"
                );
            
            }
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
        // The registry client should have used the cached version of 'resource-caps' response and should not not have called the 'resource-caps' endpoint again.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-one/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        
        //
        // The registry client should not have called the other registries.
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-two/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        
        mockServer.verify(
            request()
                .withPath(
                    "/good-registry-three/resource-caps"
                    ),
                VerificationTimes.exactly(0)
        );        

        //
        // The registry client should have called the 'missing-capabilities' endpoint twice, once to try to populate the cache,
        // and a second time to try to get the content without using the cache.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/missing-capabilities"
                    ),
                VerificationTimes.exactly(2)
            );        
     }         
}