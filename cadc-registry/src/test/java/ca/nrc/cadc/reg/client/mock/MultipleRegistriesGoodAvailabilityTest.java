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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.ClearType;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import ca.nrc.cadc.net.HttpGet;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import ca.nrc.cadc.xml.XmlUtil;

/**
 * A set of Junit tests that use a MockServer to test a how the RegistryClient handles good responses to queries for service availability. 
 * See https://www.mock-server.com/
 * 
 */
public class MultipleRegistriesGoodAvailabilityTest
    {
    private static final Logger log = Logger.getLogger(MultipleRegistriesGoodAvailabilityTest.class);
    static {
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.DEBUG);
    }

    private static ClientAndServer mockServer;
    @BeforeClass
    public static void startMockServer() {
        mockServer = startClientAndServer(1080);
    }
    @Before
    public void resetMockServer() {
        mockServer.reset();
    }
    @AfterClass
    public static void stopMockServer() {
        mockServer.stop();
    }

    public MultipleRegistriesGoodAvailabilityTest() {
        super();
    }
 
    @Test
    public void testGoodServiceAvailibilityOnce()
        throws Exception {

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
                    .withStatusCode(
                        HttpStatus.SC_OK
                        )
                    .withBody(
                        "ivo://good.authority/good-service = http://localhost:1080/good-service/good-capabilities"
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
                        .withStatusCode(
                            HttpStatus.SC_OK
                            )
                        .withBody(
                            "ivo://good.authority/good-service = http://localhost:1080/good-service/good-capabilities"
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
                        .withStatusCode(
                            HttpStatus.SC_OK
                            )
                        .withBody(
                            "ivo://good.authority/good-service = http://localhost:1080/good-service/good-capabilities"
                            )
            );

        //
        // Setup the 'good-capabilities' response.
        mockServer.when(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    )
                )
            .respond(
                response()
                    .withStatusCode(
                        HttpStatus.SC_OK
                        )
                    .withHeader(
                        new Header(
                            HttpHeaders.CONTENT_TYPE, MediaType.XML_UTF_8.toString()
                            )
                        )
                    .withBody(
                          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<vosi:capabilities"
                        + "    xmlns:vosi=\"http://www.ivoa.net/xml/VOSICapabilities/v1.0\""
                        + "    xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v1.0\""
                        + "    xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.1\""
                        + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "  <capability standardID=\"ivo://ivoa.net/std/VOSI#capabilities\">"
                        + "    <interface xsi:type=\"vs:ParamHTTP\" role=\"std\">"
                        + "      <accessURL use=\"full\">http://localhost:1080/good-service/good-capabilities</accessURL>"
                        + "    </interface>"
                        + "  </capability>"
                        + "  <capability standardID=\"ivo://ivoa.net/std/VOSI#availability\">"
                        + "    <interface xsi:type=\"vs:ParamHTTP\" role=\"std\">"
                        + "      <accessURL use=\"full\">http://localhost:1080/good-service/good-availability</accessURL>"
                        + "    </interface>"
                        + "  </capability>"
                        + "</vosi:capabilities>"
                        )
                    );

        //
        // Setup the 'good-availability' response.
        mockServer.when(
            request()
                .withPath(
                    "/good-service/good-availability"
                    )
                )
            .respond(
                response()
                    .withStatusCode(
                        HttpStatus.SC_OK
                        )
                    .withHeader(
                        new Header(
                            HttpHeaders.CONTENT_TYPE, MediaType.XML_UTF_8.toString()
                            )
                        )
                    .withBody(
                          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<vosi:availability"
                        + "    xmlns:vosi=\"http://www.ivoa.net/xml/VOSIAvailability/v1.0\">"
                        + "  <vosi:available>true</vosi:available>"
                        + "  <vosi:note>this service is available</vosi:note>"
                        + "</vosi:availability>"
                        )
                    );
                    
        
        //
        // Setup the good configuration properties.
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
        RegistryClient regClient = new RegistryClient(configFile);
        regClient.delteCache();
        
        //
        // Get the good service availability.
        try {
            URL endpoint = regClient.getServiceURL(
                new URI("ivo://good.authority/good-service"),
                new URI("ivo://ivoa.net/std/VOSI#availability"),
                null
                );
            assertNotNull(
                endpoint
                );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            HttpGet download = new HttpGet(endpoint, outputStream);
            download.run();
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            Document document = XmlUtil.buildDocument(
                    inputStream,
                null
                );
            assertEquals(
                "availability",
                document.getRootElement().getName()
                );
            assertEquals(
                "http://www.ivoa.net/xml/VOSIAvailability/v1.0",
                document.getRootElement().getNamespace().getURI()
                );
            ;
            
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
        // The registry client calls the 'resource-caps' endpoint on the first registry once to get the list of 'capabilities' endpoints.
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
        // The registry client calls the 'good-capabilities endpoint once to get the service capabilities.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(1)
            );        

        //
        // The JUnit test calls the 'good-availability' endpoint once to get the service availability.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-availability"
                    ),
                VerificationTimes.exactly(1)
            );        
    }         

    
    @Test
    public void testGoodServiceAvailibilityTwice()
        throws Exception {

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
                    .withStatusCode(
                        HttpStatus.SC_OK
                        )
                    .withBody(
                        "ivo://good.authority/good-service = http://localhost:1080/good-service/good-capabilities"
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
                        .withStatusCode(
                            HttpStatus.SC_OK
                            )
                        .withBody(
                            "ivo://good.authority/good-service = http://localhost:1080/good-service/good-capabilities"
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
                        .withStatusCode(
                            HttpStatus.SC_OK
                            )
                        .withBody(
                            "ivo://good.authority/good-service = http://localhost:1080/good-service/good-capabilities"
                            )
            );

        //
        // Setup the 'good-capabilities' response.
        mockServer.when(
            request()
                .withPath("/good-service/good-capabilities")
                )
            .respond(
                response()
                    .withStatusCode(
                        HttpStatus.SC_OK
                        )
                    .withHeader(
                        new Header(
                            HttpHeaders.CONTENT_TYPE, MediaType.XML_UTF_8.toString()
                            )
                        )
                    .withBody(
                          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<vosi:capabilities"
                        + "    xmlns:vosi=\"http://www.ivoa.net/xml/VOSICapabilities/v1.0\""
                        + "    xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v1.0\""
                        + "    xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.1\""
                        + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "  <capability standardID=\"ivo://ivoa.net/std/VOSI#capabilities\">"
                        + "    <interface xsi:type=\"vs:ParamHTTP\" role=\"std\">"
                        + "      <accessURL use=\"full\">http://localhost:1080/good-service/good-capabilities</accessURL>"
                        + "    </interface>"
                        + "  </capability>"
                        + "  <capability standardID=\"ivo://ivoa.net/std/VOSI#availability\">"
                        + "    <interface xsi:type=\"vs:ParamHTTP\" role=\"std\">"
                        + "      <accessURL use=\"full\">http://localhost:1080/good-service/good-availability</accessURL>"
                        + "    </interface>"
                        + "  </capability>"
                        + "</vosi:capabilities>"
                        )
                    );

        //
        // Setup the 'good-availability' response.
        mockServer.when(
            request()
                .withPath(
                    "/good-service/good-availability"
                    )
                )
            .respond(
                response()
                    .withStatusCode(
                        HttpStatus.SC_OK
                        )
                    .withHeader(
                        new Header(
                            HttpHeaders.CONTENT_TYPE, MediaType.XML_UTF_8.toString()
                            )
                        )
                    .withBody(
                          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<vosi:availability"
                        + "    xmlns:vosi=\"http://www.ivoa.net/xml/VOSIAvailability/v1.0\">"
                        + "  <vosi:available>true</vosi:available>"
                        + "  <vosi:note>this service is available</vosi:note>"
                        + "</vosi:availability>"
                        )
                    );
                    
        
        //
        // Setup the good configuration properties.
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
        RegistryClient regClient = new RegistryClient(configFile);
        regClient.delteCache();
        
        //
        // Get the good service availability.
        try {
            URL endpoint = regClient.getServiceURL(
                new URI("ivo://good.authority/good-service"),
                new URI("ivo://ivoa.net/std/VOSI#availability"),
                null
                );
            assertNotNull(
                endpoint
                );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            HttpGet download = new HttpGet(endpoint, outputStream);
            download.run();
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            Document document = XmlUtil.buildDocument(
                    inputStream,
                null
                );
            assertEquals(
                "availability",
                document.getRootElement().getName()
                );
            assertEquals(
                "http://www.ivoa.net/xml/VOSIAvailability/v1.0",
                document.getRootElement().getNamespace().getURI()
                );
            ;
            
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
        // The registry client should call the 'resource-caps' endpoint on the first registry once to get the list of 'capabilities' endpoints.
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
        // The registry client should have called the 'good-capabilities endpoint once to get the service capabilities.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(1)
            );        
        //
        // The JUnit test calls the 'good-availability' endpoint once to get the service availability.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-availability"
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
        // Get the good service availability again.
        try {
            URL endpoint = regClient.getServiceURL(
                new URI("ivo://good.authority/good-service"),
                new URI("ivo://ivoa.net/std/VOSI#availability"),
                null
                );
            assertNotNull(
                endpoint
                );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            HttpGet download = new HttpGet(endpoint, outputStream);
            download.run();
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            Document document = XmlUtil.buildDocument(
                    inputStream,
                null
                );
            assertEquals(
                "availability",
                document.getRootElement().getName()
                );
            assertEquals(
                "http://www.ivoa.net/xml/VOSIAvailability/v1.0",
                document.getRootElement().getNamespace().getURI()
                );
            ;
            
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
                    "/good-registry/resource-caps"
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
        // The registry client should have used the cached version of 'good-capabilities' response and should not not have called the 'good-capabilities' endpoint again.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-capabilities"
                    ),
                VerificationTimes.exactly(0)
            );        
        
        //
        // The JUnit test will call the 'good-availability' endpoint once.
        mockServer.verify(
            request()
                .withPath(
                    "/good-service/good-availability"
                    ),
                VerificationTimes.exactly(1)
            );        
    }         
}


