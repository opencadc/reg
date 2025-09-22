/*
 * <meta:header>
 *   <meta:licence>
 *     Copyright (C) 2025 University of Manchester.
 *
 *     This information is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This information is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   </meta:licence>
 * </meta:header>
 *
 *
 */

package ca.nrc.cadc.reg.client.mock;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.MediaType;

import ca.nrc.cadc.reg.client.RegistryClient;

/**
 * 
 */
public class MockServerTestBase
    {
    private static final Logger log = Logger.getLogger(MockServerTestBase.class);

    /**
     * 
     */
    public MockServerTestBase()
        {
        super();
        }

    public RegistryClient buildRegistryClient(final String ...endpoints)
        throws IOException {
    
        File configFile = File.createTempFile("registry-config", "properties");
        PrintWriter printWriter = new PrintWriter(
            new FileWriter(configFile)
            );
        for (String endpoint : endpoints)
            {
            printWriter.println(
                "ca.nrc.cadc.reg.client.RegistryClient.baseURL = " + endpoint 
                );
            }
        printWriter.close();
        
        RegistryClient registryClient = new RegistryClient(configFile);
        registryClient.deleteCache();

        return registryClient ;
        }

    protected static ClientAndServer mockServer;

    @BeforeClass
    public static void startMockServer() {
        mockServer = startClientAndServer(1080);
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", String.valueOf(mockServer.getPort()));
    }
    
    @AfterClass
    public static void stopMockServer() {
        mockServer.stop();
    }

    public void setupMockServer()
        throws IOException {
        
        mockServer.reset();

        //
        // Setup the good 'resource-caps' responses.
        mockServer.when(
            request()
                .withPath(
                    "/good-registry-001/resource-caps"
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
                    "/good-registry-002/resource-caps"
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
                        IOUtils.toByteArray(
                            getClass().getClassLoader().getResourceAsStream(
                                "good-capabilities.xml"
                                )
                            )
                        )
            );
        }
    }
