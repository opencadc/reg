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
import ca.nrc.cadc.net.HttpTransfer;
import ca.nrc.cadc.net.ResourceNotFoundException;
import ca.nrc.cadc.rest.InlineContentHandler;
import ca.nrc.cadc.rest.RestAction;
import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.PropertiesReader;
import ca.nrc.cadc.xml.XmlUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.opencadc.reg.oai.OAIHeader;
import org.opencadc.reg.oai.OAIWriter;

/**
 * A cadc-rest GetAction implementation that implements minimal OAI publishing.
 * 
 * @author pdowler
 */
public class OAIQueryAction extends RestAction {
    private static final Logger log = Logger.getLogger(OAIQueryAction.class);

    private static final String CONFIG = "reg.properties";
    private static final String AUTHORITY_KEY = "org.opencadc.reg.authority";

    enum OAI {
        verb, metadataPrefix, set, identifier, from, until, resumptionToken
    }

    private URL oaiEndpoint;
    private String authority;

    public OAIQueryAction() {
    }

    @Override
    protected InlineContentHandler getInlineContentHandler() {
        return null; // not used
    }

    @Override
    public void initAction() throws Exception {
        super.initAction();
        this.oaiEndpoint = new URL(syncInput.getRequestURI());
        PropertiesReader pr = new PropertiesReader(CONFIG);
        MultiValuedProperties mvp = pr.getAllProperties();
        this.authority = mvp.getFirstPropertyValue(AUTHORITY_KEY);
    }
    
    @Override
    public void doAction() throws Exception {
        
        if (authority == null) {
            String msg = "not found: OAI publishing not configured\n";
            logInfo.setSuccess(false);
            logInfo.setMessage(msg);

            syncOutput.setCode(404);
            syncOutput.setHeader(HttpTransfer.CONTENT_TYPE, "text/plain");
            Writer w = new OutputStreamWriter(syncOutput.getOutputStream());
            w.write(msg);
            w.flush();
            return;
        }

        logInfo.setSuccess(false);
        try {
            // mandatory OAI verb
            String verb = getParamValue(OAI.verb.name());
            if (verb == null) {
                logInfo.setMessage("badVerb: " + verb);
                sendError(verb, 200, "badVerb");
                return;
            }

            // content assumed small so never support partial list + resume
            String resumptionToken = getParamValue(OAI.resumptionToken.name());
            if (resumptionToken != null) {
                logInfo.setMessage("badResumptionToken: " + resumptionToken);
                sendError(verb, 200, "badResumptionToken");
                return;
            }

            try {

                String metadataPrefix = getParamValue(OAI.metadataPrefix.name());
                String set = getParamValue(OAI.set.name());
                Date start = toDate(getParamValue(OAI.from.name()));
                Date end = toDate(getParamValue(OAI.until.name()));
                switch (verb) {
                    case "Identify":
                    case "ListMetadataFormats":
                    case "ListSets":
                        doStatic(verb);
                        break;
                    case "ListIdentifiers":
                        // only one set

                        if (set != null && !"ivo_managed".equals(set)) {
                            logInfo.setMessage("noRecordsMatch: " + set);
                            sendError(verb, 200, "noRecordsMatch");
                            return;
                        }
                        if (metadataPrefix == null) {
                            throw new IllegalArgumentException("badArgument");
                        } 
                        if (start != null && end != null && start.after(end)) {
                            throw new IllegalArgumentException("noRecordsMatch");
                        }
                        
                        doListIdentifiers(start, end, metadataPrefix);
                        break;
                    case "ListRecords":
                        if (set != null && !"ivo_managed".equals(set)) {
                            logInfo.setMessage("noRecordsMatch: " + set);
                            sendError(verb, 200, "noRecordsMatch");
                            return;
                        }
                        if (metadataPrefix == null) {
                            throw new IllegalArgumentException("badArgument");
                        } 
                        if (start != null && end != null && start.after(end)) {
                            throw new IllegalArgumentException("noRecordsMatch");
                        }
                        
                        doListRecords(start, end, metadataPrefix);
                        break;
                    case "GetRecord":
                        String identifier = getParamValue(OAI.identifier.name());
                        if (identifier == null) {
                            throw new IllegalArgumentException("badArgument");
                        }
                        if (metadataPrefix == null) {
                            throw new IllegalArgumentException("badArgument");
                        }
                        try {
                            doGetRecord(identifier, metadataPrefix);
                        } catch (URISyntaxException ex) {
                            throw new IllegalArgumentException("badArgument");
                        }
                        break;
                    default:
                        logInfo.setMessage("badVerb: " + verb);
                        sendError(verb, 200, "badVerb");
                }
                logInfo.setSuccess(true);
            } catch (IllegalArgumentException | ResourceNotFoundException ex) {
                logInfo.setMessage(ex.getMessage());
                sendError(verb, 200, ex.getMessage());
            } catch (UnsupportedOperationException ex) {
                String msg = ex.getMessage();
                if (msg == null) {
                    msg = "not implemented";
                }
                logInfo.setMessage(msg);
                sendError(400, msg);
            } catch (IOException ex) {
                log.error("FAIL", ex);
                sendError(500, "failed to access content");
            }
        } catch (IOException ex) {
            // failed to send error to caller
            log.debug("failed to send error to caller", ex);
        } catch (Throwable t) {
            try {
                logInfo.setMessage(t.toString());
                log.error("FAIL", t);
                sendError(500, t.toString());
            } catch (Throwable t2) {
                log.debug("failed to send error to caller", t2);
            }
        }
    }

    private String getParamValue(String s) {
        List<String> vals = syncInput.getParameters(s);
        if (vals != null) {
            Iterator<String> iter = vals.iterator();
            if (iter.hasNext()) {
                String ret = iter.next();
                if (iter.hasNext()) {
                    throw new IllegalArgumentException("badArgument");
                }
                return ret;
            }
        }
        return null;
    }

    // unit test
    static Date toDate(String s) {
        if (s == null) {
            return null;
        }
        DateFormat df = DateUtil.getDateFormat(DateUtil.ISO8601_DATE_FORMAT_Z, DateUtil.UTC);
        
        try {
            return df.parse(s);
        } catch (ParseException oops) {
            log.warn("fail: " + oops);
        }
        try {
            // missing Z
            return df.parse(s + "Z");
        } catch (ParseException oops) {
            log.warn("fail: " + oops);
        }
        try {
            // missing time but has T
            return df.parse(s + "00:00:00Z");
        } catch (ParseException oops) {
            log.warn("fail: " + oops);
        }
        try {
            // missing time
            return df.parse(s + "T00:00:00Z");
        } catch (ParseException oops) {
            log.warn("fail: " + oops);
        }
        throw new IllegalArgumentException("badArgument");
    }
    
    private void sendError(int code, String msg) throws IOException {
        syncOutput.setCode(code);
        syncOutput.setHeader("Content-Type", "text/plain");

        PrintWriter w = new PrintWriter(syncOutput.getOutputStream());
        w.println(msg);
        w.close();
    }

    private void sendError(String oaiRequest, int code, String msg) throws IOException {
        syncOutput.setCode(code);
        syncOutput.setHeader("Content-Type", "text/xml");

        OAIWriter w = new OAIWriter();
        w.writeError(oaiEndpoint, oaiRequest, msg, syncOutput.getOutputStream());
    }

    private void doStatic(String oaiRequest) throws IOException {
        File f = getMetaResource(null, oaiRequest, true);
        if (f == null) {
            throw new RuntimeException("CONFIG: failed to find " + oaiRequest);
        }
        InputStream istream = new FileInputStream(f);

        OAIWriter w = new OAIWriter();
        syncOutput.setCode(200);
        syncOutput.setHeader("Content-Type", "text/xml");
        w.write(oaiEndpoint, oaiRequest, istream, syncOutput.getOutputStream());
    }

    private void doListIdentifiers(Date start, Date end, String metadataPrefix) throws IOException {
        log.debug("doListIdentifiers: " + start + " " + end);
        List<OAIHeader> headers = getHeaders(start, end);
        if (headers.isEmpty()) {
            sendError("ListIdentifiers", 200, "noRecordsMatch");
        }
        if (!"ivo_vor".equals(metadataPrefix)) {
            throw new IllegalArgumentException("cannotDisseminateFormat");
        }
        List<OAIHeader> out = new ArrayList<>(headers);
        OAIWriter w = new OAIWriter();
        syncOutput.setCode(200);
        syncOutput.setHeader("Content-Type", "text/xml");
        w.writeList(oaiEndpoint, "ListIdentifiers", out, syncOutput.getOutputStream());
    }

    private void doListRecords(Date start, Date end, String metadataPrefix) throws IOException {
        log.debug("doListRecords: " + start + " " + end);
        List<OAIHeader> headers = getHeaders(start, end);
        if (headers.isEmpty()) {
            sendError("ListRecords", 200, "noRecordsMatch");
        }
        if (!"ivo_vor".equals(metadataPrefix)) {
            throw new IllegalArgumentException("cannotDisseminateFormat");
        }
        List<OAIHeader> out = new ArrayList<>(headers);
        OAIWriter w = new OAIWriter();
        syncOutput.setCode(200);
        syncOutput.setHeader("Content-Type", "text/xml");
        w.writeList(oaiEndpoint, "ListRecords", out, syncOutput.getOutputStream());
    }

    private void doGetRecord(String identifier, String metadataPrefix) 
            throws IOException, ResourceNotFoundException, URISyntaxException {
        URI uri = new URI(identifier);
        if (!authority.equals(uri.getAuthority())) {
            throw new IllegalArgumentException("idDoesNotExist");
        }
        if (!"ivo_vor".equals(metadataPrefix)) {
            throw new IllegalArgumentException("cannotDisseminateFormat");
        }
        File f = getMetaResource(authority, uri.getPath().toLowerCase(), true);
        if (f == null) {
            throw new ResourceNotFoundException("idDoesNotExist");
        }
        InputStream istream = new FileInputStream(f);
        
        OAIWriter w = new OAIWriter();
        syncOutput.setCode(200);
        syncOutput.setHeader("Content-Type", "text/xml");
        w.write(oaiEndpoint, "GetRecord", istream, syncOutput.getOutputStream());
    }

    // implementation details below

    private List<OAIHeader> getHeaders(Date start, Date end) throws IOException {
        // currently supports one authority directory
        File authorityDir = getMetaResource(null, authority, false);
        if (authorityDir == null) {
            throw new IOException(String.format("Unable to find configured authority directory (%s)", authority));
        } else {
            List<OAIHeader> headers = new ArrayList<>();

            // authority record itself is a sibling of {authority} named {authority}.xml
            File authority = new File(authorityDir.getAbsolutePath() + ".xml");
            OAIHeader h = extractHeader(authority);
            h = filterByDate(h, start, end);
            if (h != null) {
                headers.add(h);
            }

            final File[] contentFiles = authorityDir.listFiles();
            if (contentFiles != null) {
                for (File f : contentFiles) {
                    if (f.isDirectory()) { // one level recursion for the stuff under /archive/{name}
                        final File[] subDirectoryFiles = f.listFiles();
                        if (subDirectoryFiles != null) {
                            for (File ff : subDirectoryFiles) {
                                h = extractHeader(ff);
                                h = filterByDate(h, start, end);
                                if (h != null) {
                                    headers.add(h);
                                }
                            }
                        }
                    } else if (f.isFile()) {
                        h = extractHeader(f);
                        h = filterByDate(h, start, end);
                        if (h != null) {
                            headers.add(h);
                        }
                    }
                }
            }
            if (headers.isEmpty()) {
                throw new IllegalArgumentException("noRecordsMatch");
            }
            Collections.sort(headers);
            return headers;
        }
    }

    private OAIHeader extractHeader(File f) {
        try {
            log.debug("extractHeader: " + f.getAbsolutePath());
            Document src = XmlUtil.buildDocument(new FileInputStream(f));
            Element root = src.getRootElement();
            Element record = root.getChild("record", Namespace.NO_NAMESPACE);
            Element header = record.getChild("header", Namespace.NO_NAMESPACE);

            String status = header.getAttributeValue("status", Namespace.NO_NAMESPACE);
            DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);
            URI id = URI.create(header.getChildText("identifier", Namespace.NO_NAMESPACE));
            String dsVal = header.getChildText("datestamp", Namespace.NO_NAMESPACE);
            if (dsVal.endsWith("Z")) {
                dsVal = dsVal.replace("Z", "");
            }
            Date ds = DateUtil.flexToDate(dsVal, df);
            OAIHeader ret = new OAIHeader(id, ds, "ivo_managed", status, f);
            log.debug("found: " + ret);
            return ret;
        } catch (Exception ex) {
            throw new RuntimeException("BUG: failed to read content source " + f.getAbsolutePath(), ex);
        }
    }

    private OAIHeader filterByDate(OAIHeader h, Date start, Date end) {
        if ((start == null || start.compareTo(h.getDatestamp()) <= 0)
                && (end == null || 0 <= end.compareTo(h.getDatestamp()))) {
            return h;
        }
        return null;
    }

    private File getMetaResource(String sub, String name, boolean xml) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (sub != null) {
            sb.append(sub);
        }
        sb.append(name);
        if (xml) {
            sb.append(".xml");
        }
        String filename = sb.toString();
        
        // look for content in {user.home}/config/content
        File dir = new File(System.getProperty("user.home") + "/config/content");
        File f = new File(dir, filename);
        log.debug("look for: " + f.getAbsolutePath());
        if (f.exists()) {
            return f;
        }
        return null;
    }
}
