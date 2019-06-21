/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2018.                            (c) 2018.
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

package org.opencadc.reg.oai;

import ca.nrc.cadc.date.DateUtil;
import ca.nrc.cadc.xml.XmlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;


/**
 * Write a VOResource record wrapped in an OAI-PMH document.
 *
 * @author pdowler
 */
public class OAIWriter {

    private static final Logger log = Logger.getLogger(OAIWriter.class);

    static final String OAI_NS = "http://www.openarchives.org/OAI/2.0/";
    static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String OAI_DATETIME_FMT = DateUtil.ISO8601_DATE_FORMAT_Z;

    public OAIWriter() {
    }

    public void write(URL oaiEndpoint, String oaiRequest, InputStream contentSource, OutputStream ostream)
            throws IOException {
        write(oaiEndpoint, oaiRequest, contentSource, new OutputStreamWriter(ostream));
    }

    public void write(URL oaiEndpoint, String oaiRequest, InputStream contentSource, Writer writer) throws IOException {
        Document doc = createEnvelopeOAI(oaiEndpoint, oaiRequest);
        addContent(doc, oaiRequest, contentSource);
        doOutput(doc, writer);
    }

    public void writeList(URL oaiEndpoint, String oaiRequest, List<OAIHeader> headers, OutputStream ostream)
            throws IOException {
        writeList(oaiEndpoint, oaiRequest, headers, new OutputStreamWriter(ostream));
    }

    public void writeList(URL oaiEndpoint, String oaiRequest, List<OAIHeader> headers, Writer writer)
            throws IOException {
        Document doc = createEnvelopeOAI(oaiEndpoint, oaiRequest);
        addContent(doc, oaiRequest, headers);
        doOutput(doc, writer);
    }

    public void writeError(URL oaiEndpoint, String oaiRequest, String ecode, OutputStream ostream) throws IOException {
        writeError(oaiEndpoint, oaiRequest, ecode, new OutputStreamWriter(ostream));
    }

    public void writeError(URL oaiEndpoint, String oaiRequest, String ecode, Writer writer) throws IOException {
        Document doc = createEnvelopeOAI(oaiEndpoint, null);
        Element root = doc.getRootElement();
        Namespace oai = root.getNamespace();
        Element req = new Element("error", oai);
        req.setAttribute("code", ecode, Namespace.NO_NAMESPACE);
        if ("badVerb".equals(ecode)) {
            req.setText(oaiRequest);
        }
        root.addContent(req);
        doOutput(doc, writer);
    }

    private void doOutput(Document doc, Writer writer) throws IOException {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(org.jdom2.output.Format.getPrettyFormat());
        outputter.output(doc, writer);
    }

    private Document createEnvelopeOAI(URL oaiEndpoint, String oaiRequest) {
        Namespace oai = Namespace.getNamespace(OAI_NS);
        //Namespace xsd = Namespace.getNamespace("xsd", XSD_NS);
        Namespace xsi = Namespace.getNamespace("xsi", XSI_NS);

        Element root = new Element("OAI-PMH", oai);
        //root.addNamespaceDeclaration(xsd);
        root.addNamespaceDeclaration(xsi);
        root.setAttribute("schemaLocation",
                          "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd", xsi);

        Document ret = new Document();
        ret.addContent(root);

        DateFormat df = DateUtil.getDateFormat(OAI_DATETIME_FMT, DateUtil.UTC);
        Element rd = new Element("responseDate", oai);
        rd.addContent(df.format(new Date()));
        root.addContent(rd);

        Element req = new Element("request", oai);
        if (oaiRequest != null) { // non-error scenario
            req.setAttribute("verb", oaiRequest, Namespace.NO_NAMESPACE);
        }
        req.addContent(oaiEndpoint.toExternalForm());
        root.addContent(req);

        return ret;
    }

    private void recursiveSetNS(Element e, Namespace ns) {
        if (e.getNamespace() != null && !Namespace.NO_NAMESPACE.equals(e.getNamespace())) {
            log.debug("recursiveSetNS: stop at " + e.getNamespace());
            return;
        }
        e.setNamespace(ns);
        for (Element c : e.getChildren()) {
            recursiveSetNS(c, ns);
        }
    }

    private void addContent(Document doc, String oaiRequest, InputStream src) {

        Element root = doc.getRootElement();
        Namespace oai = root.getNamespace();

        Element reqContent = getRequestElement(src);
        if (!oaiRequest.equals(reqContent.getName())) {
            throw new RuntimeException(
                    "BUG: content does not match OAI verb: " + oaiRequest + " != " + reqContent.getName());
        }
        Element req = new Element(oaiRequest, oai);
        root.addContent(req);
        ListIterator<Element> iter = reqContent.getChildren().listIterator();
        while (iter.hasNext()) {
            Element e = iter.next();
            iter.remove();
            e.detach();
            recursiveSetNS(e, oai);
            req.addContent(e);
        }

        // reqContent may contain URLs that are currently production values in the input documents
        // TODO: we could at least fix the OAI endpoint in Identify.baseURL but fixing capabilities docs? meh
    }

    private void addContent(Document doc, String oaiRequest, List<OAIHeader> headers) {
        Element root = doc.getRootElement();
        Namespace oai = root.getNamespace();
        DateFormat df = DateUtil.getDateFormat(OAI_DATETIME_FMT, DateUtil.UTC);
        if ("ListIdentifiers".equals(oaiRequest)) {
            Element reqContent = new Element("ListIdentifiers", oai);
            root.addContent(reqContent);
            for (OAIHeader h : headers) {
                log.debug("adding: " + h);
                Element he = new Element("header", oai);
                reqContent.addContent(he);

                Element id = new Element("identifier", oai);
                id.addContent(h.getIdentifier().toASCIIString());
                he.addContent(id);

                Element ds = new Element("datestamp", oai);
                ds.addContent(df.format(h.getDatestamp()));
                he.addContent(ds);

                // optional and this is not schema-valid for some reason...
                //Element ss = new Element("setSpec", oai);
                //ss.addContent(h.getSetSpec());
                //he.addContent(ss);
            }
        } else if ("ListRecords".equals(oaiRequest) && headers != null) {
            Element reqContent = new Element("ListRecords", oai);
            root.addContent(reqContent);
            for (OAIHeader h : headers) {
                log.debug("adding: " + h);
                try {
                    Element rec = getRecord(h.getInputStream());
                    recursiveSetNS(rec, oai);
                    reqContent.addContent(rec);
                    // rec may contain URLs that are currently production values in the input documents
                    // TODO: we could at least fix the OAI endpoint in Identify.baseURL but fixing capabilities docs?
                    //  meh
                } catch (IOException ex) {
                    throw new RuntimeException("BUG: failed to read source record for " + h.getIdentifier());
                }
            }
        } else {
            throw new RuntimeException("bug: addContent called with invalid OAI request: " + oaiRequest);
        }
    }

    private Element getRequestElement(InputStream src) {
        try {
            Document doc = XmlUtil.buildDocument(src);
            Element root = doc.getRootElement();
            root.detach();
            return root;
        } catch (Exception ex) {
            throw new RuntimeException("failed to read content source", ex);
        }
    }

    private Element getRecord(InputStream src) {
        Element e = getRequestElement(src);
        Element rec = e.getChild("record");
        rec.detach();
        return rec;
    }
}
