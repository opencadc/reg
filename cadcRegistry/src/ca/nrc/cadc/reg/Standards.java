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

import java.net.URI;

/**
 * This class defines the constants for the strings that conform to the IVOA 
 * service standard identifiers.
 * 
 * @author yeunga
 */
public class Standards
{
    /* Standard IDs */
	// name syntax: <base service>_<feature>_<major version><minor version>_URI    

    public final static URI CRED_DELEGATE_10_URI = URI.create("ivo://ivoa.net/std/CDP#delegate-1.0");
    public final static URI CRED_PROXY_10_URI = URI.create("ivo://ivoa.net/std/CDP#proxy-1.0");

    public final static URI DATALINK_LINKS_10_URI = URI.create("ivo://ivoa.net/std/DataLink#links-1.0");

    public final static URI GMS_GROUPS_01_URI = URI.create("ivo://ivoa.net/std/GMS#groups-0.1");
    public final static URI GMS_SEARCH_01_URI = URI.create("ivo://ivoa.net/std/GMS#search-0.1");
    
    public final static URI SIA_10_URI = URI.create("ivo://ivoa.net/std/SIA");
    public final static URI SIA_QUERY_20_URI = URI.create("ivo://ivoa.net/std/SIA#query-2.0");
    //public final static URI SIA_META_2x_URI = URI.create("ivo://ivoa.net/std/SIA#metadata-2.x");

    public final static URI SODA_SYNC_10_URI = URI.create("ivo://ivoa.net/std/SODA#sync-1.0");
    public final static URI SODA_ASYNC_10_URI = URI.create("ivo://ivoa.net/std/SODA#async-1.0");

    public final static URI TAP_10_URI = URI.create("ivo://ivoa.net/std/TAP");
    public final static URI TAP_SYNC_11_URI = URI.create("ivo://ivoa.net/std/TAP#sync-1.1");
    public final static URI TAP_ASYNC_11_URI = URI.create("ivo://ivoa.net/std/TAP#async-1.1");

    public final static URI UMS_USERS_01_URI = URI.create("ivo://ivoa.net/std/UMS#users-0.1");
    public final static URI UMS_REQS_01_URI = URI.create("ivo://ivoa.net/std/UMS#reqs-0.1");
    public final static URI UMS_LOGIN_01_URI = URI.create("ivo://ivoa.net/std/UMS#login-0.1");
    public final static URI UMS_MODPASS_01_URI = URI.create("ivo://ivoa.net/std/UMS#modpass-0.1");
    public final static URI UMS_RESETPASS_01_URI = URI.create("ivo://ivoa.net/std/UMS#resetpass-0.1");
    public final static URI UMS_WHOAMI_01_URI = URI.create("ivo://ivoa.net/std/UMS#whoami-0.1");

    public final static URI VOSI_CAPABILITIES_URI = URI.create("ivo://ivoa.net/std/VOSI#capabilities");
    public final static URI VOSI_AVAILABILITY_URI = URI.create("ivo://ivoa.net/std/VOSI#availability");
    public final static URI VOSI_TABLES_URI = URI.create("ivo://ivoa.net/std/VOSI#tables");
    public final static URI VOSI_TABLES_11_URI = URI.create("ivo://ivoa.net/std/VOSI#tables-1.1");

    public final static URI VOSPACE_NODES_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#nodes");
    public final static URI VOSPACE_PROPERTIES_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#properties");
    public final static URI VOSPACE_PROTOCOLS_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#protocols");
    public final static URI VOSPACE_SEARCHES_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#searches");
    public final static URI VOSPACE_SYNC_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#sync");
    public final static URI VOSPACE_SYNC_21_URI = URI.create("ivo://ivoa.net/std/VOSpace#sync-2.1");
    public final static URI VOSPACE_TRANSFERS_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#transfers");
    public final static URI VOSPACE_VIEWS_20_URI = URI.create("ivo://ivoa.net/std/VOSpace/v2.0#views");

    public final static URI CAOM2_OBS_20_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2#obs-1.0");
    
    public final static URI CAOM2REPO_OBS_20_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#obs-1.0");
    //public final static URI CAOM2REPO_GRANTS_20_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/CAOM2Repository#grants-1.0");

    public final static URI CUTOUT_20_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VOX#cutout-2.0");
    
    public final static URI DATA_10_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/archive#file-1.0");

    public final static URI LOGGING_CONTROL_10_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Logging#control-1.0");
    
    public final static URI PKG_10_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Pkg#tar-1.0");

    public final static URI PROC_JOBS_10_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/Proc#jobs-1.0");

    //public final static URI VMOD_10_URI = URI.create("vos://cadc.nrc.ca~vospace/CADC/std/VMOD");

}
