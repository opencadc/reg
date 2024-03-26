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
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.vosi;

import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.auth.IdentityManager;
import ca.nrc.cadc.log.ServletLogInfo;
import ca.nrc.cadc.log.WebServiceLogInfo;
import ca.nrc.cadc.net.NetUtil;
import ca.nrc.cadc.util.InvalidConfigException;
import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.PropertiesReader;
import ca.nrc.cadc.util.StringUtil;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Servlet implementation of VOSI-availabilities.
 */
public class AvailabilityServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(AvailabilityServlet.class);
    private static final long serialVersionUID = 201003131300L;

    private static final String AVAILABILITY_PROPERTIES = "cadc-vosi.properties";
    private static final String MODE_KEY = "startupMode";
    private static final String USERS_PROPERTY = "user";

    private String pluginClassName;
    private String appName;

    @Override
    public void init(ServletConfig config)
            throws ServletException {
        // TECHNICAL DEBT: recreate cadc-rest appName
        this.appName = config.getServletContext().getContextPath().substring(1).replaceAll("/", "-"); 
        this.pluginClassName = config.getInitParameter(AvailabilityPlugin.class.getName());
        log.info("application: " + appName + " plugin impl: " + pluginClassName);
        
        MultiValuedProperties mvp = getAvailabilityProperties();
        String startupMode = mvp.getFirstPropertyValue(MODE_KEY);
        if (startupMode != null) {
            AvailabilityPlugin ap = loadPlugin();
            ap.setState(startupMode);
        }
    }
    
    private AvailabilityPlugin loadPlugin() throws InvalidConfigException {
        try {
            Class wsClass = Class.forName(pluginClassName);
            AvailabilityPlugin ap = (AvailabilityPlugin) wsClass.getConstructor().newInstance();
            ap.setAppName(appName);
            log.debug("loaded: " + wsClass);
            return ap;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException 
                | InstantiationException | NoSuchMethodException | SecurityException 
                | InvocationTargetException ex) {
            throw new InvalidConfigException("failed to load AvailabilityPlugin: " + pluginClassName, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean started = false;
        WebServiceLogInfo logInfo = new ServletLogInfo(request);
        long start = System.currentTimeMillis();
        try {
            Subject subject = AuthenticationUtil.getSubject(request, false);
            logInfo.setSubject(subject);
            log.info(logInfo.start());

            AvailabilityPlugin ap = loadPlugin();

            String detail = request.getParameter("detail");
            if (detail != null && detail.equals("min")) {
                if (ap.heartbeat()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                }
            } else {
                Availability avail = ap.getStatus();
                avail.clientIP = NetUtil.getClientIP(request);

                Document document = Availability.toXmlDocument(avail);
                XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
                started = true;
                response.setContentType("text/xml");
                xop.output(document, response.getOutputStream());
            }

            logInfo.setSuccess(true);
        } catch (Exception ex) {
            log.error("BUG", ex);
            if (!started) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, ex.getMessage());
            }
            logInfo.setSuccess(false);
            logInfo.setMessage(ex.toString());
        } finally {
            logInfo.setElapsedTime(System.currentTimeMillis() - start);
            log.info(logInfo.end());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebServiceLogInfo logInfo = new ServletLogInfo(request);
        long start = System.currentTimeMillis();
        try {
            Subject subject = AuthenticationUtil.getSubject(request, false);
            logInfo.setSubject(subject);
            log.info(logInfo.start());

            AvailabilityPlugin ap = loadPlugin();

            IdentityManager im = AuthenticationUtil.getIdentityManager();
            String caller = im.toDisplayString(subject);
            if (authorized(subject)) {
                String state = request.getParameter("state");
                ap.setState(state);
                log.info("WebService state change by " + caller + " [OK]");
            } else {
                log.warn("WebService state change by " + caller + " [DENIED]");
            }

            response.sendRedirect(request.getRequestURL().toString());

            logInfo.setSuccess(true);
        } catch (Throwable t) {
            log.error("BUG", t);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, t.getMessage());
            logInfo.setSuccess(false);
            logInfo.setMessage(t.toString());
        } finally {
            logInfo.setElapsedTime(System.currentTimeMillis() - start);
            log.info(logInfo.end());
        }
    }

    private boolean authorized(Subject caller) {
        if (caller != null) {
            Set<Principal> authorizedPrincipals = getAuthorizedPrincipals();
            for (Principal cp : caller.getPrincipals()) {
                for (Principal ap : authorizedPrincipals) {
                    log.debug("authorize?  " + ap.getName() + " vs " + cp.getName());
                    if (AuthenticationUtil.equals(ap, cp)) {
                        log.debug("Authorized Principal: " + ap.getName());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<Principal> getAuthorizedPrincipals() {
        Set<Principal> authorizedPrincipals = new HashSet<Principal>();
        try {
            MultiValuedProperties mvp = getAvailabilityProperties();
            List<String> authorizedUsers = mvp.getProperty(USERS_PROPERTY);
            for (String authorizedUser : authorizedUsers) {
                if (StringUtil.hasLength(authorizedUser)) {
                    authorizedPrincipals.add(new X500Principal(authorizedUser));
                }
            }
        } catch (IllegalArgumentException e) {
            log.debug("No authorized users configured");
        }
        log.debug("configured aithorized: " + authorizedPrincipals.size());
        return authorizedPrincipals;
    }

    private MultiValuedProperties getAvailabilityProperties() {
        PropertiesReader reader = new PropertiesReader(AVAILABILITY_PROPERTIES);
        if (!reader.canRead()) {
            return new MultiValuedProperties();
        }
        return reader.getAllProperties();
    }

}
