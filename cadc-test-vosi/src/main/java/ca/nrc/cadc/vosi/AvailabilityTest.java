/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2019.                            (c) 2019.
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
import ca.nrc.cadc.net.HttpDownload;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import ca.nrc.cadc.vosi.avail.CheckWebService;
import ca.nrc.cadc.xml.XmlUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ContentFilter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;


/**
 * Tests the availability of a service.
 */
public class AvailabilityTest {
    private static final Logger log = Logger.getLogger(AvailabilityTest.class);

    private final URI resourceIdentifier;

    static {
        Log4jInit.setLevel("ca.nrc.cadc.vosi", Level.INFO);
    }

    public AvailabilityTest() {
        this(null);
    }

    public AvailabilityTest(URI resourceIdentifier) {
        if (resourceIdentifier == null) {
            // get resourceIdentifier from system property
            String resourceIdentifierName = AvailabilityTest.class.getName() + ".resourceIdentifier";
            String resourceIdentifierValue = System.getProperty(resourceIdentifierName);
            log.debug(resourceIdentifierName + "=" + resourceIdentifierValue);
            this.resourceIdentifier = URI.create(resourceIdentifierValue);
        } else {
            this.resourceIdentifier = resourceIdentifier;
        }
    }

    @Test
    public void testAvailability() {
        try {
            CheckWebService checkWebService = new CheckWebService(lookupServiceURL().toString());
            checkWebService.check();
        } catch (Throwable t) {
            log.error("unexpected exception", t);
            Assert.fail("unexpected exception: " + t);
        }
    }

    @Test
    public void testHeartBeat() {
        try {
            String heartbeatURLString = lookupServiceURL().toString() + "?detail=min";
            URL url = new URL(heartbeatURLString);
            URLConnection conn = url.openConnection();
            int code = ((HttpURLConnection) conn).getResponseCode();
            if (code == HttpServletResponse.SC_OK) {
                log.debug("test succeeded: " + heartbeatURLString);
            } else {
                log.debug("test failed: " + heartbeatURLString);
                throw new RuntimeException("heart beat test failed with status code " + code);
            }
        } catch (Throwable t) {
            log.error("unexpected exception", t);
            Assert.fail("unexpected exception: " + t);
        }
    }

    @Test
    public void testClientIP() throws Exception {
        final URL serviceURL = lookupServiceURL();
        final AvailabilityClient availabilityClient = new AvailabilityClient();
        final ByteArrayOutputStream outputStream = availabilityClient.getOutputStream();
        final HttpDownload download = availabilityClient.getHttpDownload(serviceURL, outputStream);
        download.run();

        final Document xml =
            XmlUtil.buildDocument(outputStream.toString("UTF-8"),
                                  VOSI.AVAILABILITY_NS_URI, VOSI.AVAILABILITY_SCHEMA);
        Assert.assertTrue("Should have comment.", hasClientIPComment(xml.getRootElement()));
    }

    URL lookupServiceURL() {
        RegistryClient regClient = new RegistryClient();
        URL availabilityURL = regClient.getServiceURL(resourceIdentifier, Standards.VOSI_AVAILABILITY, AuthMethod.ANON);
        log.info("availability url: " + availabilityURL);

        return availabilityURL;
    }

    /**
     * Obtain whether the given Document contains a Comment containing the given string.
     *
     * @param element The element to check.
     * @return True if one or more comments match, or False otherwise.
     */
    boolean hasClientIPComment(final Element element) throws Exception {
        final List<Content> comments = element.getContent(new ContentFilter(ContentFilter.COMMENT) {
            @Override
            public Content filter(final Object obj) {
                final Content c = super.filter(obj);
                return ((c != null) && (c.getValue().indexOf("</clientip>") > -1) &&
                        (c.getValue().indexOf("</clientip>") > c.getValue().indexOf("<clientip>"))) ?  c : null;
            }
        });

        return !comments.isEmpty();
    }
}
