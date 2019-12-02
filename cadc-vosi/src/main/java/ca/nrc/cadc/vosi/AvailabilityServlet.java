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

import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.log.ServletLogInfo;
import ca.nrc.cadc.log.WebServiceLogInfo;
import ca.nrc.cadc.net.NetUtil;

import ca.nrc.cadc.util.PropertiesReader;
import ca.nrc.cadc.util.StringUtil;
import java.io.IOException;
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
 * Servlet implementation class CapabilityServlet
 */
public class AvailabilityServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(AvailabilityServlet.class);
    private static final long serialVersionUID = 201003131300L;

    private static final String AVAILABILITY_PROPERTIES = "availabilityProperties";
    private static final String USERS_PROPERTY = "users";

    private String pluginClassName;
    private String appName;
    private String availabilityProperties;

    @Override
    public void init(ServletConfig config)
        throws ServletException {
        this.appName = config.getServletContext().getServletContextName();
        this.pluginClassName = config.getInitParameter(AvailabilityPlugin.class.getName());
        log.info("application: " + appName + " plugin impl: " + pluginClassName);

        // get the logControl.properties file for this service if it exists
        availabilityProperties = config.getInitParameter(AVAILABILITY_PROPERTIES);
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

            Class wsClass = Class.forName(pluginClassName);
            AvailabilityPlugin ap = (AvailabilityPlugin) wsClass.newInstance();
            ap.setAppName(appName);

            String detail = request.getParameter("detail");
            if (detail != null && detail.equals("min")) {
                if (ap.heartbeat()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                }
            } else {
                AvailabilityStatus status = ap.getStatus();

                Availability availability = new Availability(status);
                availability.setClientIP(NetUtil.getClientIP(request));

                Document document = availability.toXmlDocument();
                XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
                started = true;
                response.setContentType("text/xml");
                xop.output(document, response.getOutputStream());
            }

            logInfo.setSuccess(true);
        } catch (Throwable t) {
            log.error("BUG", t);
            if (!started) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, t.getMessage());
            }
            logInfo.setSuccess(false);
            logInfo.setMessage(t.toString());
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

            Class wsClass = Class.forName(pluginClassName);
            AvailabilityPlugin ap = (AvailabilityPlugin) wsClass.newInstance();
            ap.setAppName(appName);

            Principal caller = AuthenticationUtil.getX500Principal(subject);
            if (authorized(caller)) {
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

    /**
     * Checks if the caller Principal matches an authorized Principal
     * from the properties file.
     *
     * @param caller The calling Principal
     * @return true if the calling Principal matches an authorized principal.
     */
    private boolean authorized(Principal caller) {
        if (caller != null) {
            Set<Principal> authorizedPrincipals = getAuthorizedPrincipals();
            for (Principal authorizedPrincipal : authorizedPrincipals) {
                if (AuthenticationUtil.equals(authorizedPrincipal, caller)) {
                    log.debug("Authorized Principal: " + authorizedPrincipal.getName());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get a Set of X500Principal's from the availability properties. Return
     * an empty set if the properties do not exist or can't be read.
     *
     * @return Set of X500Principals, can be an empty Set if there are no authorized principals.
     */
    private Set<Principal> getAuthorizedPrincipals() {
        Set<Principal> authorizedPrincipals = new HashSet<Principal>();
        PropertiesReader propertiesReader = getAvailabilityProperties();
        if (propertiesReader != null) {
            try {
                List<String> authorizedUsers = propertiesReader.getPropertyValues(USERS_PROPERTY);
                for (String authorizedUser : authorizedUsers) {
                    if (StringUtil.hasLength(authorizedUser)) {
                        authorizedPrincipals.add(new X500Principal(authorizedUser));
                    }
                }
            } catch (IllegalArgumentException e) {
                log.debug("No authorized users configured");
            }
        }
        return authorizedPrincipals;
    }

    /**
     * Read the availability properties file and returns a PropertiesReader.
     *
     * @return A PropertiesReader, or null if the properties file does not
     *          exist or can not be read.
     */
    private PropertiesReader getAvailabilityProperties() {
        PropertiesReader reader = null;
        if (availabilityProperties != null) {
            reader = new PropertiesReader(availabilityProperties);
            if (!reader.canRead()) {
                reader = null;
            }
        }
        return reader;
    }

}
