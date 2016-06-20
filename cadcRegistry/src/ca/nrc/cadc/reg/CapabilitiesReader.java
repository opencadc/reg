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

import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.xml.XmlUtil;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser to setup the schema map for parsing a VOSI-capabilities document.
 * 
 * @author yeunga
 */
public class CapabilitiesReader 
{
    private static final Logger log = Logger.getLogger(CapabilitiesReader.class);
    
    private String resourceIDStr = "";
    private String standardIDStr = "";
    private String accessURLStr = "";
    private String securityMethodStr = "";
    protected Map<String,String> schemaMap;
    
    public CapabilitiesReader()
    {
        this(true);
    }
    
    public CapabilitiesReader(boolean enableSchemaValidation)
    {
        if (enableSchemaValidation)
        {
            this.schemaMap = new HashMap<String,String>();
            String url;

            url = XmlUtil.getResourceUrlString(RegistryClient.CAPABILITIES_SCHEMA, CapabilitiesReader.class);
            if (url != null)
            {
                log.debug(RegistryClient.CAPABILITIES_NS_URI + " -> " + url);
                schemaMap.put(RegistryClient.CAPABILITIES_NS_URI, url);
            }
            else
                log.warn("failed to find resource: " + RegistryClient.CAPABILITIES_SCHEMA);

            url = XmlUtil.getResourceUrlString(RegistryClient.VORESOURCE_SCHEMA, CapabilitiesReader.class);
            if (url != null)
            {
                log.debug(RegistryClient.VORESOURCE_NS_URI + " -> " + url);
                schemaMap.put(RegistryClient.VORESOURCE_NS_URI, url);
            }
            else
                log.warn("failed to find resource: " + RegistryClient.VORESOURCE_SCHEMA);

            url = XmlUtil.getResourceUrlString(RegistryClient.VODATASERVICE_SCHEMA, CapabilitiesReader.class);
            if (url != null)
            {
                log.debug(RegistryClient.VODATASERVICE_NS_URI + " -> " + url);
                schemaMap.put(RegistryClient.VODATASERVICE_NS_URI, url);
            }
            else
                log.warn("failed to find resource: " + RegistryClient.VODATASERVICE_SCHEMA);
            
            url = XmlUtil.getResourceUrlString(RegistryClient.STC_SCHEMA, CapabilitiesReader.class);
            if (url != null)
            {
                log.debug(RegistryClient.STC_NS_URI + " -> " + url);
                schemaMap.put(RegistryClient.STC_NS_URI, url);
            }
            else
                log.warn("failed to find resource: " + RegistryClient.STC_SCHEMA);

            url = XmlUtil.getResourceUrlString(XmlUtil.XSI_SCHEMA, CapabilitiesReader.class);
            if (url != null)
            {
                log.debug(XmlUtil.XSI_NS_URI + " -> " + url);
                schemaMap.put(XmlUtil.XSI_NS_URI, url);
            }
            else
                log.warn("failed to find resource: " + RegistryClient.XLINK_SCHEMA);
            
            url = XmlUtil.getResourceUrlString(RegistryClient.XLINK_SCHEMA, CapabilitiesReader.class);
            if (url != null)
            {
                log.debug(RegistryClient.XLINK_NS_URI + " -> " + url);
                schemaMap.put(RegistryClient.XLINK_NS_URI, url);
            }
            else
                log.warn("failed to find resource: " + RegistryClient.XLINK_SCHEMA);
        }
    }
    
    /**
     * Add an additional schema to the parser configuration. This is needed if the VOSI-capabilities
     * uses an extension schema for xsi:type.
     * 
     * @param namespace
     * @param schemaLocation 
     */
    public void addSchemaLocation(String namespace, String schemaLocation)
    {
        log.debug("addSchemaLocation: " + namespace + " -> " + schemaLocation);
        schemaMap.put(namespace, schemaLocation);
    }

    /**
     *  Construct a Capabilities from an XML String source.
     *
     * @param xml String of the XML.
     * @return Capabiltiies Capabilities.
     */
    public Capabilities read(String xml) 
    {
        if (xml == null)
        {
            throw new IllegalArgumentException("XML must not be null");
        }
        
        return read(new StringReader(xml));
    }

    /**
     * Construct a Capabilities from a InputStream.
     *
     * @param in InputStream.
     * @return Capabilities Capabilities.
     * @throws IOException 
     */
    public Capabilities read(InputStream istream)
    {
        if (istream == null)
        {
            throw new RuntimeException("capabilities xml file stream closed");
        }

        try
        {
            return read(new InputStreamReader(istream, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding not supported");
        }
    }
    
    public Capabilities read(Reader reader)
    {
        if (reader == null)
        {
            throw new IllegalArgumentException("reader must not be null");
        }

        // Create a JDOM Document from the XML
        Document document;
        try
        {
            document = XmlUtil.buildDocument(reader, schemaMap);
        }
        catch (IOException ioe)
        {
            String msg = "Error reading XML: " + ioe.getMessage();
            throw new RuntimeException(msg, ioe);
        }
        catch (JDOMException jde)
        {
            String msg = "XML failed schema validation: " + jde.getMessage();
            throw new RuntimeException(msg, jde);
        }

        return this.buildCapabilities(document.getRootElement());
    }

    private Capabilities buildCapabilities(final Element root) 
    {
    	Capabilities caps = new Capabilities(this.parseResourceID(root));
   	    List<Element> capElementList = root.getChildren("capability");
   	    for (Element capElement : capElementList)
   	    {
   	    	Capability cap = this.buildCapability(capElement);
   	    	caps.getCapabilities().add(cap);
   	    }
   	    
   	    return caps;
    }
    
    private Capability buildCapability(final Element capElement) 
    {
    	Capability cap = new Capability(this.parseStandardID(capElement));
    	List<Element> intfElementList = capElement.getChildren("interface");
    	for (Element intfElement : intfElementList)
    	{
    		Interface intf = this.buildInterface(intfElement);
    		cap.getInterfaces().add(intf);
    	}
    	
    	return cap;
    }
    
    private Interface buildInterface(final Element intfElement) 
    {
    	AccessURL accessURL = this.parseAccessURL(intfElement.getChild("accessURL"));
    	URI securityMethod = this.parseSecurityMethod(intfElement.getChild("securityMethod"));
    	String roleString = intfElement.getAttributeValue("role");
    	Interface intf = new Interface(accessURL, securityMethod);
    	intf.role = roleString;
    	return intf;
    }
    
    private URI parseSecurityMethod(final Element securityMethodElement) 
    {
    	String standardIDString = securityMethodElement.getAttributeValue("standardID");
    	if (standardIDString == null)
    	{
    		String prefix = this.resourceIDStr + this.standardIDStr + this.accessURLStr;
    		String msg = prefix + ", standardID attribute not found in securityMethod element";
    		throw new RuntimeException(msg);
    	}
    	
    	this.securityMethodStr = ", securityMethod standardID=" + standardIDString;
    	URI standardID;
		try 
		{
			standardID = new URI(standardIDString);
		} 
		catch (URISyntaxException e) 
		{
    		String prefix = this.resourceIDStr + this.standardIDStr + this.accessURLStr + this.securityMethodStr;
			String msg = prefix + ", invalid securityMethod standardID in xml: " + e.getMessage();
            throw new RuntimeException(msg);
		}
		
    	log.debug("securityMethod standardID: " + standardIDString);    	
        return standardID;
    }
    
    private AccessURL parseAccessURL(final Element accessURLElement) 
    {
    	AccessURL accessURL = new AccessURL(this.parseURL(accessURLElement));
    	accessURL.use = this.parseUse(accessURLElement);
    	return accessURL;
    }
    
    private String parseUse(final Element accessURLElement) 
    {
    	String useString = accessURLElement.getAttributeValue("use");
    	if (useString == null)
    	{
    		String msg = this.resourceIDStr + this.standardIDStr + this.accessURLStr + ", use attribute not found in accessURL element";
    		throw new RuntimeException(msg);
    	}
    	
    	log.debug("accessURL use: " + useString);    	
        return useString;
    }
    
    private URL parseURL(final Element accessURLElement) 
    {
    	String accessURLString = accessURLElement.getText();
    	if (accessURLString == null)
    	{
    		String msg = this.resourceIDStr + this.standardIDStr + ", URL not found in accessURL element";
    		throw new RuntimeException(msg);
    	}
    	
    	log.debug("accessURL: " + accessURLString);  
    	this.accessURLStr = ", accessURL=" + accessURLString;
    	
    	URL accessURL;
		try 
		{
			accessURL = new URL(accessURLString);
		} 
		catch (MalformedURLException e) 
		{
			String msg = this.resourceIDStr + this.standardIDStr + this.accessURLStr + ", invalid accessURL in xml: " + e.getMessage();
            throw new RuntimeException(msg);
		}
        return accessURL;
    }
   
    private URI parseStandardID(final Element capElement) 
    {
    	String standardIDString = capElement.getAttributeValue("standardID");
    	if (standardIDString == null)
    	{
    		String msg = this.resourceIDStr + ", standardID attribute not found in capability element";
    		throw new RuntimeException(msg);
    	}
    	
    	this.standardIDStr = ", standardID=" + standardIDString;
    	URI standardID;
    	
		try 
		{
			standardID = new URI(standardIDString);
		} 
		catch (URISyntaxException e) 
		{
			String msg = this.resourceIDStr + this.standardIDStr + ", invalid standardID in xml: " + e.getMessage();
            throw new RuntimeException(msg);
		}
		
    	log.debug("capabilities standardID: " + standardIDString);    
        return standardID;
    }
    
    private URI parseResourceID(final Element root) 
    {
    	String resourceIDString = root.getAttributeValue("resourceID");
    	if (resourceIDString == null)
    	{
    		String msg = "resourceID attribute not found in capabilities element";
    		throw new RuntimeException(msg);
    	}
    	
    	this.resourceIDStr = "resourceID=" + resourceIDString;
    	URI resourceID;
    	
		try 
		{
			resourceID = new URI(resourceIDString);
		} 
		catch (URISyntaxException e) 
		{
			String msg = this.resourceIDStr + ", invalid resourceID in xml: " + e.getMessage();
            throw new RuntimeException(msg);
		}
		
    	log.debug("capabilities resourceID: " + resourceIDString);    
        return resourceID;
    }
}
