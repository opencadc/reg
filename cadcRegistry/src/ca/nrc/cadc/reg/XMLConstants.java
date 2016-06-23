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
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Namespace;

import ca.nrc.cadc.xml.W3CConstants;
import ca.nrc.cadc.xml.XmlUtil;

/**
 * This class defines the URI and corresponding namespace constants. 
 * 
 * @author yeunga
 */
public class XMLConstants
{
    public static final URI CAPABILITIES_NS_URI;
    public static final URI STC_NS_URI;
    public static final URI TABLES_NS_URI;
    public static final URI TAPREGEXT_NS_URI;
    public static final URI VODATASERVICE_NS_URI;
    public static final URI VORESOURCE_NS_URI;    
    public static final URI XLINK_NS_URI;
    
    static 
    {
        try 
        {
            CAPABILITIES_NS_URI = URI.create("http://www.ivoa.net/xml/VOSICapabilities/v1.0");
            STC_NS_URI = URI.create("http://www.ivoa.net/xml/STC/stc-v1.30.xsd");
            TABLES_NS_URI = URI.create("http://www.ivoa.net/xml/VOSITables/v1.0");
            TAPREGEXT_NS_URI = URI.create("http://www.ivoa.net/xml/TAPRegExt/v1.0");
            VODATASERVICE_NS_URI = URI.create("http://www.ivoa.net/xml/VODataService/v1.1");
            VORESOURCE_NS_URI = URI.create("http://www.ivoa.net/xml/VOResource/v1.0");
            XLINK_NS_URI = URI.create("http://www.w3.org/1999/xlink");
        } 
        catch(IllegalArgumentException bug)
        {
            throw new RuntimeException("BUG: invalid URI string in static constants", bug);
        }
        catch(NullPointerException bug)
        {
            throw new RuntimeException("BUG: null URI string in static constants", bug);
        }
    }    

    private static final String CAPABILITIES_SCHEMA = "VOSICapabilities-v1.0.xsd";
    private static final String STC_SCHEMA = "STC-v1.3.xsd";
    private static final String TABLES_SCHEMA = "VOSITables-v1.0.xsd";
    private static final String TAPREGEXT_SCHEMA = "TAPRegExt-v1.0.xsd";
    private static final String VODATASERVICE_SCHEMA = "VODataService-v1.1.xsd";    
    private static final String VORESOURCE_SCHEMA = "VOResource-v1.0.xsd";    
    private static final String XLINK_SCHEMA = "XLINK.xsd";
        
    public static final Map<URI,String> SCHEMA_MAP = new HashMap<URI,String>();
    static
    {
        SCHEMA_MAP.put(CAPABILITIES_NS_URI, CAPABILITIES_SCHEMA);
        SCHEMA_MAP.put(STC_NS_URI, STC_SCHEMA);
        SCHEMA_MAP.put(TABLES_NS_URI, TABLES_SCHEMA);
        SCHEMA_MAP.put(TAPREGEXT_NS_URI, TAPREGEXT_SCHEMA);
        SCHEMA_MAP.put(VODATASERVICE_NS_URI, VODATASERVICE_SCHEMA);
        SCHEMA_MAP.put(VORESOURCE_NS_URI, VORESOURCE_SCHEMA);
        SCHEMA_MAP.put(XLINK_NS_URI, XLINK_SCHEMA);
        SCHEMA_MAP.put(W3CConstants.XSI_NS_URI, W3CConstants.XSI_SCHEMA);
    }
    
    public static final Namespace CAPABILITIES_NS = Namespace.getNamespace("vosi", CAPABILITIES_NS_URI.toString());
    public static final Namespace TABLES_NS = Namespace.getNamespace("vosi", TABLES_NS_URI.toString());
    public static final Namespace VODATASERVICE_NS = Namespace.getNamespace("vod", VODATASERVICE_NS_URI.toString());

    /**
     * Get the schema map that maps a namespace URI string to the file path
     * of the corresponding schema file.
     * @return schema map
     */
    public static Map<String,String> getSchemaMap()
    {
    	Map<String,String> map = new HashMap<String,String>();
    	
        for (Map.Entry<URI,String> es : SCHEMA_MAP.entrySet())
        {
        	map.put(es.getKey().toString(), XmlUtil.getResourceUrlString(es.getValue(), XMLConstants.class));
        }
        
        return map;
    }
    
    /**
     * Get the namespace URI corresponding to the specified URI String.
     * @param uriString a namespace URI string
     * @return the corresponding namespace URI if found, null if not found
     */
    public static URI getURI(final String uriString)
    {
    	URI retURI = null;
    	
        for (URI uri : SCHEMA_MAP.keySet())
        {
            if (uri.toString().equals(uriString))
            {
                retURI = uri;
            }
        }
        
        return retURI;
    }
    
    /**
     * Get the namespace URI corresponding to the specified schema.
     * @param schema a string representing a schema
     * @return the corresponding namespace URI if found, null if not found
     */
    public static URI getURIBySchema(final String schema)
    {
    	URI retURI = null;
    	
        for (Map.Entry<URI,String> es : SCHEMA_MAP.entrySet())
        {
            if ( es.getValue().equals(schema))
            {
                retURI = es.getKey();
            }
        }
        
        return retURI;
    }
    
    /**
     * Get the schema corresponding to the specified namespace URI.
     * @param uri a namespace URI
     * @return the corresponding schema if found, null if not found
     */
    public static String getSchema(final URI uri)
    {
        return SCHEMA_MAP.get(uri);
    }

}
