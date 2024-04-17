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

package org.opencadc.reg.server;

import ca.nrc.cadc.log.ServletLogInfo;
import ca.nrc.cadc.log.WebServiceLogInfo;
import ca.nrc.cadc.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Simple servlet that performs a canned query to the registry and returns a
 * list of resourceID(s) and the accessURL of the associated VOSI-capabilities
 * endpoint.
 * For access to resource capabilities for web services, add this to a web.xml:
 * &lt;init-param&gt;
 * &lt;param-name&gt;queryFile&lt;/param-name&gt;
 * &lt;param-value&gt;(query file name as found in /config directory on server)&lt;/param-value&gt;
 * &lt;/init-param&gt;
 *
 * @author jeevesh
 */
public class CannedQueryServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(CannedQueryServlet.class);

    private static String PARAM_QUERY_FILE = "queryFile";
    
    private String configFileName;
    
    public CannedQueryServlet() {
    }

    static File checkFileExists(String paramFileName) {
        File f = new File(System.getProperty("user.home") + "/config/" + paramFileName);
        if (f.exists()) {
            return f;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
        this.configFileName = config.getInitParameter(PARAM_QUERY_FILE);

        log.info("initCannedQuery: checking for " + configFileName);
        File f = checkFileExists(configFileName);
        if (f != null) {
            if (f.canRead()) {
                log.info("initCannedQuery: " + configFileName + " OK");
            } else {
                log.error("initCannedQuery: " + configFileName + " not readable FAIL");
            }
        } else {
            log.info("initCannedQuery: " + configFileName + " not found DISABLED");
            this.configFileName = null; // disabled
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        WebServiceLogInfo logInfo = new ServletLogInfo(request);
        long start = System.currentTimeMillis();

        try {
            log.info(logInfo.start());
            if (configFileName != null) {
                File f = checkFileExists(configFileName);
                byte[] buf = FileUtil.readFile(f);
                response.setStatus(200);
                response.setContentType("text/plain");
                response.setContentLength(buf.length);
                
                OutputStream ostream = response.getOutputStream();
                ostream.write(buf);
                ostream.flush();
                logInfo.setSuccess(true);
            } else {
                String msg = "not found: feature not configured\n";
                logInfo.setSuccess(false);
                logInfo.setMessage(msg);
                
                response.setStatus(404);
                response.setContentType("text/plain");
                Writer w = response.getWriter();
                w.write(msg);
                w.flush();
            }
        } catch (Throwable t) {
            logInfo.setSuccess(false);
            logInfo.setMessage(t.toString());
            log.error("BUG: failed to deliver file content", t);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
        } finally {
            logInfo.setElapsedTime(System.currentTimeMillis() - start);
            log.info(logInfo.end());
        }
    }

}
