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
public class Standards {

    // standardID values
    public static final URI CRED_DELEGATE_10 = URI.create("ivo://ivoa.net/std/CDP#delegate-1.0");
    public static final URI CRED_PROXY_10 = URI.create("ivo://ivoa.net/std/CDP#proxy-1.0");

    // DALI 1.1 Examples
    public static final URI DALI_EXAMPLES_11 = URI.create("ivo://ivoa.net/std/DALI#examples");

    public static final URI DATALINK_LINKS_10 = URI.create("ivo://ivoa.net/std/DataLink#links-1.0");
    public static final URI DATALINK_LINKS_11 = URI.create("ivo://ivoa.net/std/DataLink#links-1.1");

    public static final URI GMS_GROUPS_01 = URI.create("ivo://ivoa.net/std/GMS#groups-0.1");
    
    @Deprecated
    public static final URI GMS_SEARCH_01 = URI.create("ivo://ivoa.net/std/GMS#search-0.1");
    public static final URI GMS_SEARCH_10 = URI.create("ivo://ivoa.net/std/GMS#search-1.0");

    public static final URI REGISTRY_10 = URI.create("ivo://ivoa.net/std/Registry");

    public static final URI SIA_10 = URI.create("ivo://ivoa.net/std/SIA");
    public static final URI SIA_QUERY_20 = URI.create("ivo://ivoa.net/std/SIA#query-2.0");
    public static final URI DAP_QUERY_21 = URI.create("ivo://ivoa.net/std/DAP#query-2.1");
    //public static final URI SIA_META_2x = URI.create("ivo://ivoa.net/std/SIA#metadata-2.x");

    // Simple Cone Search 1.1
    public static final URI SCS_11 = URI.create("ivo://ivoa.net/std/conesearch#query-1.1");

    public static final URI SODA_SYNC_10 = URI.create("ivo://ivoa.net/std/SODA#sync-1.0");
    public static final URI SODA_ASYNC_10 = URI.create("ivo://ivoa.net/std/SODA#async-1.0");

    public static final URI TAP_10 = URI.create("ivo://ivoa.net/std/TAP");

    @Deprecated
    public static final URI UMS_USERS_01 = URI.create("ivo://ivoa.net/std/UMS#users-0.1");
    @Deprecated
    public static final URI UMS_REQS_01 = URI.create("ivo://ivoa.net/std/UMS#reqs-0.1");
    @Deprecated
    public static final URI UMS_LOGIN_01 = URI.create("ivo://ivoa.net/std/UMS#login-0.1");
    @Deprecated
    public static final URI UMS_RESETPASS_01 = URI.create("ivo://ivoa.net/std/UMS#resetpass-0.1");
    @Deprecated
    public static final URI UMS_WHOAMI_01 = URI.create("ivo://ivoa.net/std/UMS#whoami-0.1");
    
    public static final URI UMS_USERS_10 = URI.create("http://www.opencadc.org/std/UMS#users-1.0");
    public static final URI UMS_REQS_10 = URI.create("http://www.opencadc.org/std/UMS#reqs-1.0");
    public static final URI UMS_LOGIN_10 = URI.create("http://www.opencadc.org/std/UMS#login-1.0");
    public static final URI UMS_RESETPASS_10 = URI.create("http://www.opencadc.org/std/UMS#resetpass-1.0");
    public static final URI UMS_WHOAMI_10 = URI.create("http://www.opencadc.org/std/UMS#whoami-1.0");

    // SRCNet prototypes
    public static final URI POSIX_GROUPMAP = URI.create("http://www.opencadc.org/std/posix#group-mapping-0.1");
    public static final URI POSIX_USERMAP = URI.create("http://www.opencadc.org/std/posix#user-mapping-0.1");
    
    public static final URI VOSI_CAPABILITIES = URI.create("ivo://ivoa.net/std/VOSI#capabilities");
    public static final URI VOSI_AVAILABILITY = URI.create("ivo://ivoa.net/std/VOSI#availability");
    public static final URI VOSI_TABLES = URI.create("ivo://ivoa.net/std/VOSI#tables");
    public static final URI VOSI_TABLES_11 = URI.create("ivo://ivoa.net/std/VOSI#tables-1.1");

    public static final URI PROTO_TABLE_UPDATE_ASYNC = URI.create("ivo://ivoa.net/std/VOSI#table-update-async-1.x");
    public static final URI PROTO_TABLE_UPDATE_SYNC = URI.create("ivo://ivoa.net/std/VOSI#table-update-sync-1.x");
    public static final URI PROTO_TABLE_LOAD_SYNC = URI.create("ivo://ivoa.net/std/VOSI#table-load-sync-1.x");
    public static final URI PROTO_TABLE_PERMISSIONS = URI.create("ivo://ivoa.net/std/VOSI#table-permissions-1.x");

    public static final URI VOSPACE_NODES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#nodes");
    public static final URI VOSPACE_PROPERTIES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#properties");
    public static final URI VOSPACE_PROTOCOLS_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#protocols");
    public static final URI VOSPACE_SEARCHES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#searches");
    public static final URI VOSPACE_SYNC_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#sync");
    public static final URI VOSPACE_SYNC_21 = URI.create("ivo://ivoa.net/std/VOSpace#sync-2.1");
    public static final URI VOSPACE_TRANSFERS_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#transfers");
    public static final URI VOSPACE_VIEWS_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#views");
    @Deprecated
    public static final URI VOSPACE_FILES_20 = URI.create("ivo://ivoa.net/std/VOSpace/v2.x#files");
    
    public static final URI VOSPACE_FILES = URI.create("ivo://ivoa.net/std/VOSpace#files-proto");
    public static final URI VOSPACE_RECURSIVE_DELETE = URI.create("ivo://ivoa.net/std/VOSpace#recursive-delete-proto");
    public static final URI VOSPACE_RECURSIVE_NODEPROPS = URI.create("ivo://ivoa.net/std/VOSpace#recursive-nodeprops-proto");

    @Deprecated
    public static final URI VOSPACE_XFER_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOSpace#xfer");
    @Deprecated
    public static final URI VOSPACE_NODEPROPS_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOSpace#nodeprops");

    public static final URI CAOM2_OBS_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2#obs-1.0");

    public static final URI CAOM2REPO_OBS_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#obs-1.0");
    public static final URI CAOM2REPO_OBS_23 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#obs-1.1");
    public static final URI CAOM2REPO_OBS_24 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#obs-1.2");
    public static final URI CAOM2REPO_DEL_23 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#del-1.0");
    
    @Deprecated
    public static final URI CAOM2REPO_PERMS = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#perms-1.0");

    @Deprecated
    public static final URI CUTOUT_20 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOX#cutout-2.0");

    @Deprecated
    public static final URI DATA_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/archive#file-1.0");
    
    /**
     * Storage Inventory files API. Supports persistence, management, retrieval, and operations on files.
     */
    public static final URI SI_FILES = URI.create("http://www.opencadc.org/std/storage#files-1.0");
    
    /**
     * Storage Inventory locate API. Supports transfer negotiation for files.
     */
    public static final URI SI_LOCATE = URI.create("http://www.opencadc.org/std/storage#locate-1.0"); // approximately VOSPACE_SYNC_21
    
    /**
     * Storage Inventory permissions API. Supports getting current permission info for files.
     */
    public static final URI SI_PERMISSIONS = URI.create("http://www.opencadc.org/std/storage#permissions-1.0");
    
    public static final URI LOGGING_CONTROL_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Logging#control-1.0");

    // Standard for package requests (ie zip or tar)
    public static final URI PKG_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Pkg-1.0");

    /**
     * Science Platform
     */
    // batch processing - deprecated
    @Deprecated
    public static final URI PROC_JOBS_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Proc#jobs-1.0");
    // interactive processing - deprecated
    @Deprecated
    public static final URI PROC_SESSIONS_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Proc#sessions-1.0");
    
    // science platform sessions and jobs
    public static final URI_PLATFORM_SESSION_1 = URI.create("http://www.opencadc.org/std/platform#session-1");
    // science platform context (resources)
    public static final URI_PLATFORM_CONTEXT_1 = URI.create("http://www.opencadc.org/std/platform#context-1");
    // science platform images
    public static final URI_PLATFORM_IMAGE_1 = URI.create("http://www.opencadc.org/std/platform#image-1");
    // science platform repositories
    public static final URI_PLATFORM_REPO_1 = URI.create("http://www.opencadc.org/std/platform#repository-1");
    

    public static final URI UWS_UPDATE_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/UWS#update-1.0");

    public static final URI RESOLVER_10 = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/NameResolver#names-1.0");

    public static final URI DOI_INSTANCES_10 = URI.create("http://www.opencadc.org/std/doi#instances-1.0");

    public static final URI DOI_SEARCH_10 = URI.create("http://www.opencadc.org/std/doi#search-1.0");

    // Security method standardIDs
    public static final URI SECURITY_METHOD_PASSWORD = URI.create("ivo://ivoa.net/sso#tls-with-password");
    public static final URI SECURITY_METHOD_OAUTH = URI.create("ivo://ivoa.net/sso#OAuth");
    public static final URI SECURITY_METHOD_OPENID = URI.create("ivo://ivoa.net/sso#OpenID");
    
    // Security methods
    public static final URI SECURITY_METHOD_ANON = URI.create("ivo://ivoa.net/sso#anon");
    public static final URI SECURITY_METHOD_CERT = URI.create("ivo://ivoa.net/sso#tls-with-certificate");
    public static final URI SECURITY_METHOD_COOKIE = URI.create("ivo://ivoa.net/sso#cookie");
    public static final URI SECURITY_METHOD_HTTP_BASIC = URI.create("ivo://ivoa.net/sso#BasicAA");
    public static final URI SECURITY_METHOD_TOKEN = URI.create("ivo://ivoa.net/sso#token");
    
    @Deprecated // Was for prototype delegation token work. Use SECURITY_METHOD_TOKEN now.
    public static final URI SECURITY_METHOD_DELTOKEN = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Auth#token-1.0");

    // interface type identifiers: <namespace uri>#<type attr name without prefix>
    public static URI INTERFACE_PARAM_HTTP = URI.create(XMLConstants.VODATASERVICE_11_NS + "#ParamHTTP");
    public static URI INTERFACE_REG_OAI = URI.create(XMLConstants.REGISTRY_10_NS.toASCIIString() + "#OAIHTTP");

    public static URI INTERFACE_WEB_BROWSER = URI.create(XMLConstants.VORESOURCE_10_NS + "#WebBrowser");

    private static final Map<URI, AuthMethod> SEC_MAP = new HashMap<URI, AuthMethod>();

    static {
        SEC_MAP.put(SECURITY_METHOD_ANON, AuthMethod.ANON);
        SEC_MAP.put(SECURITY_METHOD_CERT, AuthMethod.CERT);
        SEC_MAP.put(SECURITY_METHOD_COOKIE, AuthMethod.COOKIE);
        SEC_MAP.put(SECURITY_METHOD_HTTP_BASIC, AuthMethod.PASSWORD);
        SEC_MAP.put(SECURITY_METHOD_TOKEN, AuthMethod.TOKEN);
    }

    public static AuthMethod getAuthMethod(URI securityMethod) {
        return SEC_MAP.get(securityMethod);
    }
    
    public static URI getSecurityMethod(AuthMethod authMethod) {
        for (Map.Entry<URI,AuthMethod> e : SEC_MAP.entrySet()) {
            if (e.getValue().equals(authMethod)) {
                return e.getKey();
            }
        }
        return null;
    }
}
