package ca.nrc.cadc.vosi;

import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.log.ServletLogInfo;
import ca.nrc.cadc.log.WebServiceLogInfo;
import ca.nrc.cadc.net.NetUtil;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
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

    private String pluginClassName;
    private String appName;

    @Override
    public void init(ServletConfig config)
        throws ServletException {
        this.appName = config.getServletContext().getServletContextName();
        this.pluginClassName = config.getInitParameter(AvailabilityPlugin.class.getName());
        if (pluginClassName == null) {
            // backwards compat
            this.pluginClassName = config.getInitParameter(WebService.class.getName());
        }
        log.info("application: " + appName + " plugin impl: " + pluginClassName);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        boolean started = false;
        WebServiceLogInfo logInfo = new ServletLogInfo(request);
        long start = System.currentTimeMillis();
        try {
            Subject subject = AuthenticationUtil.getSubject(request);
            logInfo.setSubject(subject);
            log.info(logInfo.start());

            Class wsClass = Class.forName(pluginClassName);
            WebService ws = (WebService) wsClass.newInstance();
            if (ws instanceof AvailabilityPlugin) {
                AvailabilityPlugin ap = (AvailabilityPlugin) ws;
                ap.setAppName(appName);
            }
            
            AvailabilityStatus status = ws.getStatus();

            Availability availability = new Availability(status);
            availability.setClientIP(NetUtil.getClientIP(request));

            Document document = availability.toXmlDocument();
            XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
            started = true;
            response.setContentType("text/xml");
            xop.output(document, response.getOutputStream());

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
            Subject subject = AuthenticationUtil.getSubject(request);
            logInfo.setSubject(subject);
            log.info(logInfo.start());

            Class wsClass = Class.forName(pluginClassName);
            WebService ws = (WebService) wsClass.newInstance();
            if (ws instanceof AvailabilityPlugin) {
                AvailabilityPlugin ap = (AvailabilityPlugin) ws;
                ap.setAppName(appName);
            }

            Subject.doAs(subject, new ChangeServiceState(ws, request));

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

    private class ChangeServiceState implements PrivilegedExceptionAction {
        private WebService ws;
        private HttpServletRequest req;

        ChangeServiceState(WebService ws, HttpServletRequest req) {
            this.ws = ws;
            this.req = req;
        }

        public Object run() throws Exception {
            String state = req.getParameter("state");
            ws.setState(state);
            return null;
        }
    }
}
