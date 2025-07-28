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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.AfterClass;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;
import org.mockserver.model.XmlBody; 
import org.mockserver.model.BinaryBody;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.util.Log4jInit;
import ca.nrc.cadc.util.PropertiesReader;

/**
 * 
 */
public class ExploreClientTest
    {
    private static String TEST_CONFIG_DIR = PropertiesReader.class.getName() + ".dir";
    
    static String STANDARD_ID_IVOA_TAP = "ivo://ivoa.net/std/TAP";

    static String RESOURCE_ID_CADC_ARGUS = "ivo://cadc.nrc.ca/argus";
    static String RESOURCE_ID_SPSRC_ARGUS = "ivo://espsrc.iaa.csic.es/argus";
    
    static String RESOURCE_ID_SKAO_REG  = "ivo://skao.int/reg";
    static String RESOURCE_ID_SKAO_TEST = "ivo://skao.int/test";

    static String RESOURCE_ID_NO_AUTH_METHOD = "ivo://cadc.nrc.ca/noauthmethod";
    static String RESOURCE_ID_NOT_FOUND = "ivo://cadc.nrc.ca/notfound";

    static final String MOCKSERVER_BASE_URL = "http://localhost:1080/";
    static final String MOCKSERVER_AVAILABILITIES_PATH = "/fish/availabilities";
    static final String MOCKSERVER_CAPABILITIES_PATH = "/fish/capabilities";
    
    private static final Logger log = Logger.getLogger(ExploreClientTest.class);
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

    private static String originalUserHome = null ; 
    @Before
    public void divertUserHome()
    throws IOException {
        originalUserHome = System.getProperty("user.home");
        System.setProperty(
            "user.home",
            Files.createTempDirectory(
                this.getClass().getName()
                ).toString()
        );
    }
    @After
    public void restoreUserHome() {
        if (null != originalUserHome) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Before
    public void setConfigDir() {
        System.setProperty(TEST_CONFIG_DIR, "src/test/resources");
    }
    @After
    public void unsetConfigDir() {
        System.clearProperty(TEST_CONFIG_DIR);
    }

    public static void deleteDirectory(final String path)
        throws IOException {
        deleteDirectory(
            Paths.get(path)
            );
    }
    
    /**
     * Recursive directory delete, using FileVisitor.
     * https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileVisitor.html
     * @param path
     * @throws IOException
     * 
     */
    public static void deleteDirectory(final Path path)
        throws IOException {
        if (path.toFile().exists()) {
            Files.walkFileTree(
                path,
                new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException
                        {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                        }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e)
                        throws IOException
                        {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                            }
                        else {
                            throw e;
                            }
                        }
                    }
                );        
            }        
        }        

    /**
     * Recursive directory delete, using Stream API.
     * https://stackoverflow.com/a/35989142
     * https://stackoverflow.com/questions/35988192/java-nio-most-concise-recursive-directory-delete
     * @param root
     * @throws IOException
     * 
     */
    public void frogDirectory(final Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(System.out::println)
                .forEach(File::delete);
        }
        catch (IOException ouch)
            {
            log.debug(
                "IOException [" + ouch.getMessage()+ "]"
                );
        }    
    }
    
//  @Test
    public void testSomething()
        throws Exception {
        log.debug(
            System.getProperty("user.dir")
            );
        log.debug(
            System.getProperty("user.home")
            );
        log.debug(
            System.getProperty(TEST_CONFIG_DIR)
            );
        
        RegistryClient regClient = new RegistryClient("test-001.properties");

        Capabilities caps = regClient.getCapabilities(new URI(RESOURCE_ID_CADC_ARGUS));
        List<Capability> capList = caps.getCapabilities();
        Assert.assertTrue("Incorrect number of capabilities", capList.size() > 3);

    }         

    @Test
    public void testMockEndpoint001()
        throws Exception {

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps")
                )
            .respond(
                response()
                    .withStatusCode(404)
        );

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps")
                )
            .respond(
                response()
                    .withStatusCode(404)
        );
        
        RegistryClient regClient = new RegistryClient("test-005.properties");

        deleteDirectory(
            regClient.getBaseCacheDirectory()
            );
        
        try {
            Capabilities capabilities = regClient.getCapabilities(new URI(RESOURCE_ID_CADC_ARGUS));
            fail("Expected ResourceNotFoundException");
        }
        catch (ca.nrc.cadc.net.ResourceNotFoundException ouch) {
            log.debug(
                "Expected [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
                );
        }

        //
        // CachingFile calls each URL twice,
        // once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps"),
                VerificationTimes.exactly(2)
        );        

        //
        // CachingFile calls each URL twice,
        // once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps"),
                VerificationTimes.exactly(2)
        );        
    }         

    @Test
    public void testMockEndpoint002()
        throws Exception {

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps")
                )
            .respond(
                response()
                    .withStatusCode(404)
        );

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps")
                )
            .respond(
                response()
                    .withBody(
                        RESOURCE_ID_SKAO_TEST + " = http://localhost:1080/fish/capabilities"
                        )
        );

        mockServer.when(
                request()
                    .withMethod("GET")
                    .withPath("/fish/capabilities")
                    )
                .respond(
                    response()
                        .withStatusCode(404)
            );
        
        RegistryClient regClient = new RegistryClient("test-005.properties");

        // TODO Add clearCache() method to the client.
        deleteDirectory(
                regClient.getBaseCacheDirectory()
                );
        
        try {
            Capabilities capabilities = regClient.getCapabilities(
                new URI(RESOURCE_ID_SKAO_TEST)
                );
        }
        catch (ca.nrc.cadc.net.ResourceNotFoundException ouch) {
            log.debug(
                "Expected Exception [" + ouch.getMessage() + "]"
                );
        }
        catch (Exception ouch) {
        log.debug(
            "UnExpected Exception [" + ouch.getMessage() + "]"
            );
        }

        //
        // CachingFile calls this URL twice.
        // Once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps"),
                VerificationTimes.exactly(2)
        );        

        //
        // CachingFile calls this URL once when it calls getRemoteContent()
        // to populate the cache and gets a valid response,
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps"),
                VerificationTimes.exactly(1)
        );        

        //
        // CachingFile calls this URL twice.
        // Once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/fish/capabilities"),
                VerificationTimes.exactly(2)
            );        
    }         
    
    @Test
    public void testMockEndpoint003()
        throws Exception {

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps")
                )
            .respond(
                response()
                    .withStatusCode(404)
        );

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps")
                )
            .respond(
                response()
                    .withBody(
                        RESOURCE_ID_SKAO_TEST + " = http://localhost:1080/fish/capabilities"
                        )
        );

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/fish/capabilities")
                )
            .respond(
                response()
                    .withStatusCode(404)
            );

        File configFile = File.createTempFile("test-config", "properties");
        PrintWriter writer = new PrintWriter(
            new FileWriter(configFile)
            );
        writer.println(
            "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/frog"
            );
        writer.println(
            "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/toad"
            );
        writer.close();
        
        RegistryClient regClient = new RegistryClient(configFile);

        // TODO Add clearCache() method to the client.
        deleteDirectory(
            regClient.getBaseCacheDirectory()
            );
        
        try {
            Capabilities capabilities = regClient.getCapabilities(new URI(RESOURCE_ID_SKAO_TEST));
        }
        catch (ca.nrc.cadc.net.ResourceNotFoundException ouch) {
            log.debug(
                "Expected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }
        catch (IOException ouch) {
            log.debug(
                "Expected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }
        catch (Exception ouch) {
            fail(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }

        //
        // CachingFile calls this URL twice.
        // Once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps"),
                VerificationTimes.exactly(2)
        );        

        //
        // CachingFile calls this URL once when it calls getRemoteContent()
        // to populate the cache and gets a valid response,
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps"),
                VerificationTimes.exactly(1)
        );        

        //
        // CachingFile calls this URL twice.
        // Once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/fish/capabilities"),
                VerificationTimes.exactly(2)
            );        
    }         
    

    
    
    
    @Test
    public void testMockEndpoint004()
        throws Exception {

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps")
                )
            .respond(
                response()
                    .withStatusCode(404)
        );

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps")
                )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody(
                        RESOURCE_ID_SKAO_TEST + " = http://localhost:1080/fish/capabilities"
                        )
        );

        mockServer.when(
            request()
                .withMethod("GET")
                .withPath("/fish/capabilities")
                )
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeader(
                        new Header(
                            HttpHeaders.CONTENT_TYPE,
                            MediaType.XML_UTF_8.toString()
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
                        + "      <accessURL use=\"full\">http://localhost:1080/fish/capabilities</accessURL>"
                        + "    </interface>"
                        + "  </capability>"
                        + "  <capability standardID=\"ivo://ivoa.net/std/VOSI#availability\">"
                        + "    <interface xsi:type=\"vs:ParamHTTP\" role=\"std\">"
                        + "      <accessURL use=\"full\">http://localhost:1080/fish/availability</accessURL>"
                        + "    </interface>"
                        + "  </capability>"
                        + "</vosi:capabilities>"
                        )
                    );

        File configFile = File.createTempFile("test-config", "properties");
        PrintWriter printWriter = new PrintWriter(
            new FileWriter(configFile)
            );
        printWriter.print(
              "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/frog"
            + "\n"
            + "ca.nrc.cadc.reg.client.RegistryClient.baseURL = http://localhost:1080/toad"
            + "\n"
            );
        printWriter.close();
        
        RegistryClient regClient = new RegistryClient(configFile);

        // TODO Add clearCache() method to the client.
        deleteDirectory(
            regClient.getBaseCacheDirectory()
            );
        
        try {
            Capabilities capabilities = regClient.getCapabilities(new URI(RESOURCE_ID_SKAO_TEST));
            List<Capability> list = capabilities.getCapabilities();
            assertTrue(
                list.size() == 2
                );
        }
        catch (Exception ouch) {
            fail(
                "Unexpected exception [" + ouch.getClass().getSimpleName() + "][" + ouch.getMessage() + "]"
            );
        }

        //
        // CachingFile calls this URL twice.
        // Once when it calls getRemoteContent() to populate the cache,
        // and again when calls getRemoteContent() if the cache fails.
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/frog/resource-caps"),
                VerificationTimes.exactly(2)
        );        

        //
        // CachingFile calls this URL once when it calls getRemoteContent()
        // to populate the cache and gets a valid response,
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/toad/resource-caps"),
                VerificationTimes.exactly(1)
        );        

        //
        // CachingFile calls this URL once when it calls getRemoteContent()
        // to populate the cache and gets a valid response,
        mockServer.verify(
            request()
                .withMethod("GET")
                .withPath("/fish/capabilities"),
                VerificationTimes.exactly(1)
            );        

    
    }         
    
    
//  @Test
    public void testCanadianEndpoint()
        throws Exception {
        
        RegistryClient regClient = new RegistryClient("test-002.properties");

        Capabilities caps = regClient.getCapabilities(new URI(RESOURCE_ID_CADC_ARGUS));
        List<Capability> capList = caps.getCapabilities();
        Assert.assertTrue("Incorrect number of capabilities", capList.size() > 3);

    }         
    
//  @Test
    public void testSpanishEndpoint()
        throws Exception {
        
        RegistryClient regClient = new RegistryClient("test-003.properties");

        Capabilities caps = regClient.getCapabilities(new URI(RESOURCE_ID_SPSRC_ARGUS));
        List<Capability> capList = caps.getCapabilities();
        Assert.assertTrue("Incorrect number of capabilities", capList.size() > 3);

    }         

    
    
}
