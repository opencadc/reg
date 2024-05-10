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

import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.net.HttpGet;
import ca.nrc.cadc.reg.Capabilities;
import ca.nrc.cadc.reg.Capability;
import ca.nrc.cadc.reg.Interface;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.util.Log4jInit;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
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

    static {
        Log4jInit.setLevel("org.opencadc.reg", Level.INFO);
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
    }
    
    private final URL oaiEndpoint;
    private final DateFormat dateFormat = DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_Z, DateUtil.UTC);
    
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
            HttpGet get = new HttpGet(u, bos);
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
            HttpGet get = new HttpGet(u, bos);
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
            HttpGet get = new HttpGet(u, bos);
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
    
    private class IdentRecord implements Comparable<IdentRecord> {
        Date datestamp;
        URI resourceID;

        public IdentRecord(Date datestamp, URI resourceID) {
            this.datestamp = datestamp;
            this.resourceID = resourceID;
        }

        @Override
        public int compareTo(IdentRecord t) {
            int ret = datestamp.compareTo(t.datestamp);
            if (ret != 0) {
                return ret;
            }
            return resourceID.compareTo(t.resourceID);
        }
    }

    // list all records in datestamp order in a map<datesmap,resourceID>
    private SortedSet<IdentRecord> listRecords() throws Exception {
        URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListIdentifiers&metadataPrefix=ivo_vor");
        log.info(u.toExternalForm());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HttpGet get = new HttpGet(u, bos);
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
        
        SortedSet<IdentRecord> ret = new TreeSet<>();
        for (Element h : headers) {
            String id = h.getChildText("identifier", ns);
            String datestamp = h.getChildText("datestamp", ns);
            String status = h.getAttributeValue("status", Namespace.NO_NAMESPACE);
            log.info("testListIdentifiers: " + datestamp + " " + id + " " + status);
            Assert.assertNotNull(id);
            Assert.assertNotNull(datestamp);
            URI resourceID = new URI(id);
            Date d = dateFormat.parse(datestamp);
            IdentRecord ir = new IdentRecord(d, resourceID);
            ret.add(ir);
        }
        return ret;
    }

    // this assumes that at least 2 seconds separates each record timestamp
    private Date[] getReducedFromUntil(SortedSet<IdentRecord> allRecords) {
        Iterator<IdentRecord> all = allRecords.iterator();
        Assert.assertTrue(all.hasNext());
        final IdentRecord first = all.next();
        
        Assert.assertTrue(all.hasNext());
        final IdentRecord r2 = all.next();
        
        Assert.assertTrue(all.hasNext());
        IdentRecord penultimate = r2;
        IdentRecord ultimate = all.next();
        while (all.hasNext()) {
            penultimate = ultimate;
            ultimate = all.next();
        }

        // compute mid
        long t1 = (first.datestamp.getTime() + r2.datestamp.getTime()) / 2L; // between 1 and 2 
        long t2 = (penultimate.datestamp.getTime() + ultimate.datestamp.getTime()) / 2L; // between N-1 and N
        // truncate to seconds
        t1 = (t1 / 1000L) * 1000L;
        t2 = (t2 / 1000L) * 1000L;
        // shift by eps
        t1 = t1 + 1000L;
        t2 = t2 - 1000L;
        return new Date[] { new Date(t1), new Date(t2) };
    }

    @Test
    public void testListIdentifiers() {
        try {
            listRecords();
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListIdentifiersEmpty() {
        try {
            // TODO: use listRecords, get oldest datestamp, and make until= param before that? or just 1984 is fine
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListIdentifiers&metadataPrefix=ivo_vor&until=1984-01-01");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(u, bos);
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
            SortedSet<IdentRecord> allRecords = listRecords();
            log.info("all records: " + allRecords.size());
            if (allRecords.size() < 3) {
                log.warn("found " + allRecords.size() + " records: unable to test ListIdentifiers with from & until");
                return;
            }
            Date[] range = getReducedFromUntil(allRecords);
            String from = dateFormat.format(range[0]);
            String until = dateFormat.format(range[1]);
            
            // figure out expected number of records between the dates
            int expectedNum = 0;
            for (IdentRecord ir: allRecords) {
                Date d = ir.datestamp;
                if (range[0].getTime() <= d.getTime()
                        && d.getTime() <= range[1].getTime()) {
                    expectedNum++;
                    log.debug("testListIdentifiersFromUntil expect: " + expectedNum + " " + ir.resourceID);
                } else {
                    log.debug("testListIdentifiersFromUntil skip: " + ir.resourceID);
                }
            }
            
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListIdentifiers&metadataPrefix=ivo_vor"
                    + "&from=" + from + "&until=" + until);
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(u, bos);
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
            
            // check: should have N-2
            Namespace ns = root.getNamespace();
            Element li = root.getChild("ListIdentifiers", ns);
            Assert.assertNotNull(li);
            List<Element> headers = li.getChildren("header", ns);
            Assert.assertFalse(headers.isEmpty());

            int found = 0;
            for (Element h : headers) {
                found++;
                String id = h.getChildText("identifier", ns);
                String datestamp = h.getChildText("datestamp", ns);
                String status = h.getAttributeValue("status", Namespace.NO_NAMESPACE);
                log.info("testListIdentifiersFromUntil: " + found + " " + datestamp + " " + id + " " + status);
                Assert.assertNotNull(id);
                Assert.assertNotNull(datestamp);
                URI resourceID = new URI(id);
                Date d = dateFormat.parse(datestamp);
            }
            Assert.assertEquals("found expected num records", expectedNum, found);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testGetRecord() {
        // currently: schema validation only
        try {
            SortedSet<IdentRecord> allRecords = listRecords();
            for (IdentRecord ir: allRecords) {
                URL u = new URL(oaiEndpoint.toExternalForm() 
                    + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + ir.resourceID.toASCIIString());
                log.info(u.toExternalForm());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                HttpGet get = new HttpGet(u, bos);
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

                // status: active or deleted
                String status = header.getAttributeValue("status");
                Assert.assertTrue(status == null || "deleted".equals(status));
                
                if (status == null) {
                    Element metadata = record.getChild("metadata", ns);
                    Assert.assertNotNull(metadata);
                
                    // TODO: further validation of the Resource??
                }
            }
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListRecordsEmpty() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListRecords&metadataPrefix=ivo_vor&until=1984-01-01");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(u, bos);
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
            Namespace ns = root.getNamespace();
            
            Element error = root.getChild("error", ns);
            Assert.assertNotNull("OAI error", error);
            String code = error.getAttributeValue("code", Namespace.NO_NAMESPACE);
            Assert.assertEquals("noRecordsMatch", code);
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testListRecords() {
        try {
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListRecords&metadataPrefix=ivo_vor");
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(u, bos);
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
    public void testListRecordsFrom() {
        try {
            SortedSet<IdentRecord> allRecords = listRecords();
            if (allRecords.size() < 3) {
                log.warn("found " + allRecords.size() + " records: unable to test ListRecords with from & until");
                return;
            }
            Date[] range = getReducedFromUntil(allRecords);
            String from = dateFormat.format(range[0]);
            String until = dateFormat.format(range[1]);
            
            URL u = new URL(oaiEndpoint.toExternalForm() + "?verb=ListRecords&metadataPrefix=ivo_vor&from=" + from);
            log.info(u.toExternalForm());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(u, bos);
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
