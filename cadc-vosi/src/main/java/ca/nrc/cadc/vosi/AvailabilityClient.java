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
import ca.nrc.cadc.net.HttpGet;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.XMLConstants;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.xml.XmlUtil;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;

public class AvailabilityClient {

    private static Logger log = Logger.getLogger(AvailabilityClient.class);

    public static final String AVAILABILITY_ENDPOINT = "/availability";
    public static final String MIN_DETAIL_PARAMETER = "?detail=min";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 6000;
    public static final int DEFAULT_READ_TIMEOUT = 12000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final int MIN_DETAIL_READ_TIMEOUT = 2000;
    public static final Map<String, String> AVAIL_SCHEMA_MAP = new TreeMap<>();

    static {
        AVAIL_SCHEMA_MAP.putAll(XMLConstants.SCHEMA_MAP);
        
        String localURL = XmlUtil.getResourceUrlString(VOSI.AVAILABILITY_SCHEMA, XMLConstants.class);
        AVAIL_SCHEMA_MAP.put(VOSI.AVAILABILITY_NS_URI.toString(), localURL);
    }

    private final URI resourceID;
    private final boolean minDetail;
    private final RegistryClient reg = new RegistryClient();

    public AvailabilityClient(URI resourceID) {
        if (resourceID == null) {
            throw new IllegalArgumentException("resourceID is null");
        }
        this.resourceID = resourceID;
        this.minDetail = false;
    }

    /**
     * Create a client to check the availability of the given resouceID.
     * 
     * @param resourceID the service resourceID
     * @param minDetail if true checks that the service is running, 
     *                  if false checks that the service is fully functional. 
     */
    public AvailabilityClient(URI resourceID, boolean minDetail) {
        if (resourceID == null) {
            throw new IllegalArgumentException("resourceID is null");
        }
        this.resourceID = resourceID;
        this.minDetail = minDetail;
    }

    @Deprecated
    public AvailabilityClient() {
        this.resourceID = null;
        this.minDetail = false;
    }

    public Availability getAvailability() {
        if (resourceID == null) {
            throw new IllegalStateException("caller used deprecated constructor - must use deprecated getAvailability(URL)");
        }
        URL avail = reg.getServiceURL(resourceID, Standards.VOSI_AVAILABILITY, AuthMethod.ANON);
        return doit(avail);
    }

    @Deprecated
    public Availability getAvailability(final URL baseURL) {
        if (baseURL == null) {
            throw new IllegalArgumentException("null URL");
        }

        try {
            URL availabilityURL = new URL(baseURL.toExternalForm() + AVAILABILITY_ENDPOINT);
            return doit(availabilityURL);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("invalid baseURL: cannot append " + AVAILABILITY_ENDPOINT, ex);
        }
    }

    private Availability doit(URL availabilityURL) {
        Availability ret;
        try {
            if (this.minDetail) {
                availabilityURL = new URL(availabilityURL.toString() + MIN_DETAIL_PARAMETER);
            }

            log.debug("GET " + availabilityURL);
            HttpGet get = new HttpGet(availabilityURL, true);
            get.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
            get.setMaxRetries(DEFAULT_MAX_RETRIES);
            if (this.minDetail) {
                get.setReadTimeout(MIN_DETAIL_READ_TIMEOUT);
            } else {
                get.setReadTimeout(DEFAULT_READ_TIMEOUT);
            }
            get.prepare();
            log.debug("GET " + availabilityURL + " code: " + get.getResponseCode());
            
            if (get.getResponseCode() == 200) {
                if (this.minDetail) {
                    return new Availability(true);
                } else {
                    Document xml = XmlUtil.buildDocument(get.getInputStream(), AVAIL_SCHEMA_MAP);
                    ret = new Availability(xml);
                }
            } else {
                ret = getFalseAvailability("unexpected response code (" + get.getResponseCode() + ") from " + availabilityURL.toExternalForm());
            }
        } catch (JDOMException ex) {
            ret = getFalseAvailability("Error parsing availability from " + availabilityURL + " reason: " + ex.getMessage());
        } catch (Exception ex) {
            ret = getFalseAvailability("Error getting availability from " + availabilityURL + " reason: " + ex.getMessage());
        }
        return ret;
    }

    protected Availability getFalseAvailability(String msg) {
        return new Availability(false, msg);
    }
}
