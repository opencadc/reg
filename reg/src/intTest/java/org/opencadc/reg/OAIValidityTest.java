/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2024.                            (c) 2024.
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
************************************************************************
*/

package org.opencadc.reg;

import ca.nrc.cadc.net.HttpDownload;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Interface;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.util.Log4jInit;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Assert;
import org.junit.Test;
import org.opencadc.reg.oai.OAIReader;

/**
 *
 * @author pdowler
 */
public class OAIValidityTest {
    private static final Logger log = Logger.getLogger(OAIValidityTest.class);

    private static final String DELETED_RESOURCE = "ivo://example.net/deleted";
    
    static {
        Log4jInit.setLevel("org.opencadc.reg", Level.INFO);
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
    }
    
    final URL oaiEndpoint;
    
    public OAIValidityTest() { 
        try {
            Capabilities caps = VosiCapabilitiesTest.getCapabilities();
            Capability oai = caps.findCapability(Standards.REGISTRY_10);
            Interface iface = oai.findInterface(Standards.SECURITY_METHOD_ANON, Standards.INTERFACE_REG_OAI);
            this.oaiEndpoint = iface.getAccessURL().getURL();
        } catch (Exception ex) {
            throw new RuntimeException("CONFIG: failed to find OAI endpoint", ex);
        }
    }
    
    @Test
    public void testIdentify() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=Identify");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListMetadataFormats() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListMetadataFormats");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListSets() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListSets");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListIdentifiers() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListIdentifiers&metadataPrefix=ivo_vor");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
            Namespace ns = root.getNamespace();
            
            Element li = root.getChild("ListIdentifiers", ns);
            Assert.assertNotNull(li);
            List<Element> headers = li.getChildren("header", ns);
            Assert.assertFalse(headers.isEmpty());
            for (Element h : headers) {
                String id = h.getChildText("identifier", ns);
                String status = h.getAttributeValue("status", Namespace.NO_NAMESPACE);
                log.info("testListIdentifiers: " + id + " status: " + status);
                Assert.assertNotNull(id);
                
                if (DELETED_RESOURCE.equals(id)) {
                    Assert.assertEquals("deleted", status);
                } else {
                    Assert.assertNull(status);
                }
            }
            
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListIdentifiersEmpty() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListIdentifiers&metadataPrefix=ivo_vor&until=1999-01-01");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNotNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListIdentifiersFromUntil() {
        try {
            String from = "2020-01-01T00:00:00Z";
            String until = "2020-02-02T00:00:00Z";
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListIdentifiers&metadataPrefix=ivo_vor"
                    + "&from=" + from + "&until=" + until);
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testGetRecord() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=ivo://example.net/registry");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
            Namespace ns = root.getNamespace();
            
            Element req = root.getChild("request", ns);
            Assert.assertNotNull(req);
            
            Assert.assertEquals("GetRecord", req.getAttributeValue("verb"));
            Element gr = root.getChild("GetRecord", ns);
            Assert.assertNotNull(gr);
            Element record = gr.getChild("record", ns);
            Assert.assertNotNull(record);
            Element header = record.getChild("header", ns);
            Assert.assertNotNull(header);

            // status: active
            Assert.assertNull(header.getAttributeValue("status"));
            Element metadata = record.getChild("metadata", ns);
            Assert.assertNotNull(metadata);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testGetDeletedRecord() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + DELETED_RESOURCE);
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
            
            Namespace ns = root.getNamespace();
            
            Element req = root.getChild("request", ns);
            Assert.assertNotNull(req);
            
            Assert.assertEquals("GetRecord", req.getAttributeValue("verb"));
            Element gr = root.getChild("GetRecord", ns);
            Assert.assertNotNull(gr);
            Element record = gr.getChild("record", ns);
            Assert.assertNotNull(record);
            Element header = record.getChild("header", ns);
            Assert.assertNotNull(header);

            // status: deleted
            Assert.assertEquals("deleted", header.getAttributeValue("status"));
            Element metadata = record.getChild("metadata", ns);
            Assert.assertNull(metadata);
            
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListRecordsFrom() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListRecords&metadataPrefix=ivo_vor&from=2019-06-15T21:15:20Z");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListRecordsEmpty() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListRecords&metadataPrefix=ivo_vor&until=1999-01-01");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNotNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testGetRecordsAll() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListRecords&metadataPrefix=ivo_vor");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpDownload get = new HttpDownload(u, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/xml", get.getContentType());
            
            String xml = bos.toString();
            log.debug("xml:\n" + xml);
            
            StringReader sr = new StringReader(xml);
            OAIReader r = new OAIReader();
            Document doc = r.read(sr);
            Element root = doc.getRootElement();
            Element error = root.getChild("error", root.getNamespace());
            Assert.assertNull("OAI error", error);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    // useful to debug schema issues since we rely on cadc-registry to provide all the IVOA xsd files
    //@Test
    public void testSchemaMap() {
        for (Map.Entry<String,String> me : ca.nrc.cadc.reg.XMLConstants.SCHEMA_MAP.entrySet()) {
            log.info("schema map: " + me.getKey() + " -> " + me.getValue());
        }
    }
}
