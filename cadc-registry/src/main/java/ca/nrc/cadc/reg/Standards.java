/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
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
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.reg;

import ca.nrc.cadc.auth.AuthMethod;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines the constants for the strings that conform to the IVOA
 * service standard identifiers.
 *
 * @author yeunga
 */
public class Standards
{
    // standardID values
    public final static URI CRED_DELEGATE_10 = URI.create("ivo://ivoa.net/std/CDP#delegate-1.0");
    public final static URI CRED_PROXY_10 = URI.create("ivo://ivoa.net/std/CDP#proxy-1.0");

    public final static URI DATALINK_LINKS_10 = URI.create("ivo://ivoa.net/std/DataLink#links-1.0");

    public final static URI GMS_GROUPS_01 = URI.create("ivo://ivoa.net/std/GMS#groups-0.1");
    public final static URI GMS_SEARCH_01 = URI.create("ivo://ivoa.net/std/GMS#search-0.1");

    public final static URI SIA_10 = URI.create("ivo://ivoa.net/std/SIA");
    public final static URI SIA_QUERY_20 = URI.create("ivo://ivoa.net/std/SIA#query-2.0");
    //public final static URI SIA_META_2x = URI.create("ivo://ivoa.net/std/SIA#metadata-2.x");

    public final static URI SODA_SYNC_10 = URI.create("ivo://ivoa.net/std/SODA#sync-1.0");
    public final static URI SODA_ASYNC_10 = URI.create("ivo://ivoa.net/std/SODA#async-1.0");

    public final static URI TAP_10 = URI.create("ivo://ivoa.net/std/TAP");
    public final static URI TAP_SYNC_11 = URI.create("ivo://ivoa.net/std/TAP#sync-1.1");
    public final static URI TAP_ASYNC_11 = URI.create("ivo://ivoa.net/std/TAP#async-1.1");

    public final static URI UMS_USERS_01 = URI.create("ivo://ivoa.net/std/UMS#users-0.1");
    public final static URI UMS_REQS_01 = URI.create("ivo://ivoa.net/std/UMS#reqs-0.1");
    public final static URI UMS_LOGIN_01 = URI.create("ivo://ivoa.net/std/UMS#login-0.1");
    public final static URI UMS_MODPASS_01 = URI.create("ivo://ivoa.net/std/UMS#modpass-0.1");
    public final static URI UMS_RESETPASS_01 = URI.create("ivo://ivoa.net/std/UMS#resetpass-0.1");
    public final static URI UMS_WHOAMI_01 = URI.create("ivo://ivoa.net/std/UMS#whoami-0.1");

    public final static URI VOSI_CAPABILITIES = URI.create("ivo://ivoa.net/std/VOSI#capabilities");
    public final static URI VOSI_AVAILABILITY = URI.create("ivo://ivoa.net/std/VOSI#availability");
    public final static URI VOSI_TABLES = URI.create("ivo://ivoa.net/std/VOSI#tables");
    public final static URI VOSI_TABLES_11 = URI.create("ivo://ivoa.net/std/VOSI#tables-1.1");

    public final static URI VOSPACE_NODES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#nodes");
    public final static URI VOSPACE_PROPERTIES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#properties");
    public final static URI VOSPACE_PROTOCOLS_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#protocols");
    public final static URI VOSPACE_SEARCHES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#searches");
    public final static URI VOSPACE_SYNC_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#sync");
    public final static URI VOSPACE_SYNC_21 = URI.create("ivo://ivoa.net/std/VOSpace#sync-2.1");
    public final static URI VOSPACE_TRANSFERS_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#transfers");
    public final static URI VOSPACE_VIEWS_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#views");

    public final static URI VOSPACE_XFER_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOSpace#xfer");
    public final static URI VOSPACE_NODEPROPS_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOSpace#nodeprops");


    public final static URI CAOM2_OBS_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2#obs-1.0");

    public final static URI CAOM2REPO_OBS_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#obs-1.0");
    public final static URI CAOM2REPO_OBS_23 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#obs-1.1");
    public final static URI CAOM2REPO_DEL_23 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#del-1.0");
    //public final static URI CAOM2REPO_GRANTS_20_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#grants-1.0");

    public final static URI CUTOUT_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOX#cutout-2.0");

    public final static URI DATA_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/archive#file-1.0");

    public final static URI LOGGING_CONTROL_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Logging#control-1.0");

    public final static URI PKG_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Pkg#tar-1.0");

    public final static URI PROC_JOBS_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Proc#jobs-1.0");

    public final static URI UWS_UPDATE_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/UWS#update-1.0");

    public final static URI RESOLVER_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/NameResolver#names-1.0");

    //public final static URI VMOD_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VMOD");

    public final static URI SECURITY_METHOD_ANON = URI.create("ivo://ivoa.net/sso#anon");
    public final static URI SECURITY_METHOD_CERT = URI.create("ivo://ivoa.net/sso#tls-with-certificate");
    public final static URI SECURITY_METHOD_COOKIE = URI.create("ivo://ivoa.net/sso#cookie");
    public final static URI SECURITY_METHOD_PASSWORD = URI.create("http://www.w3.org/Protocols/HTTP/1.0/spec.html#BasicAA");
    public final static URI SECURITY_METHOD_HTTP_BASIC = URI.create("ivo://ivoa.net/sso#BasicAA");
    public final static URI SECURITY_METHOD_TOKEN = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Auth#token-1.0");

    // interface type identifiers: <namespace uri>#<type attr name without rpefix>
    public static URI INTERFACE_PARAM_HTTP = URI.create(XMLConstants.VODATASERVICE_11_NS + "#ParamHTTP");
    public static URI INTERFACE_UWS_ASYNC = URI.create(XMLConstants.UWSREGEXT_10_NS.toASCIIString() + "#Async");
    public static URI INTERFACE_UWS_SYNC = URI.create(XMLConstants.UWSREGEXT_10_NS.toASCIIString() + "#Sync");

    private static final Map<AuthMethod,URI> SEC_MAP = new HashMap<AuthMethod,URI>();
    static
    {
        SEC_MAP.put(AuthMethod.ANON, SECURITY_METHOD_ANON);
        SEC_MAP.put(AuthMethod.CERT, SECURITY_METHOD_CERT);
        SEC_MAP.put(AuthMethod.COOKIE, SECURITY_METHOD_COOKIE);
        SEC_MAP.put(AuthMethod.PASSWORD, SECURITY_METHOD_PASSWORD);
        SEC_MAP.put(AuthMethod.TOKEN, SECURITY_METHOD_TOKEN);
    }

    public static AuthMethod getAuthMethod(URI securityMethod)
    {
        for (Map.Entry<AuthMethod,URI> me : SEC_MAP.entrySet())
        {
            if ( me.getValue().equals(securityMethod))
                return me.getKey();
        }
        throw new IllegalArgumentException("invalid value: " + securityMethod);
    }

    public static URI getSecurityMethod(AuthMethod am)
    {
        return SEC_MAP.get(am);
    }
}
