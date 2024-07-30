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

package ca.nrc.cadc.reg.client;

import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.PropertiesReader;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * Uses the library configuration file (cadc-registry.properties) to find the
 * local service that provides the specified API (via standardID).
 * 
 * @author pdowler
 */
public class LocalAuthority {

    private static final Logger log = Logger.getLogger(LocalAuthority.class);

    private static final String LOCAL_AUTH_PROP_FILE = LocalAuthority.class.getSimpleName() + ".properties";

    private Map<URI, Set<URI>> authorityMap = new TreeMap<>();

    public LocalAuthority() {
        PropertiesReader propReader = new PropertiesReader(RegistryClient.CONFIG_FILE);
        MultiValuedProperties mvp = propReader.getAllProperties();
        
        // backwards compat: try the old config file name
        if (mvp.isEmpty()) {
            propReader = new PropertiesReader(LOCAL_AUTH_PROP_FILE);
            mvp = propReader.getAllProperties();
        }

        for (String std : mvp.keySet()) {
            List<String> values = mvp.getProperty(std);
            Set<URI> vals = new HashSet<>();
            URI stdURI = URI.create(std);
            authorityMap.put(stdURI, vals);
            for (String val : values) {
                URI valURI = URI.create(val);
                log.debug("authorityMap: " + stdURI + " -> " + valURI);
                vals.add(valURI);
            }
        }
    }

    /**
     * Returns the service URI associated with the baseStandardID. This method fails if the local authority is
     * configured with more than one service URI corresponding to the baseStandard. Use `getServiceURIs` method for
     * the more generic case
     * @param baseStandardID base standard ID
     * @return corresponding service URI (http or ivo)
     * @deprecated deprecated in favour of getServiceURIs method
     */
    public URI getServiceURI(String baseStandardID) {
        Set<URI> resourceIdentifiers = authorityMap.get(URI.create(baseStandardID));
        if ((resourceIdentifiers == null) || (resourceIdentifiers.isEmpty())) {
            throw new NoSuchElementException("not found: " + baseStandardID);
        }
        if (resourceIdentifiers.size() > 1) {
            throw new NoSuchElementException("Multiple service URIs found for " + baseStandardID);
        }
        return resourceIdentifiers.iterator().next();
    }

    /**
     * Returns the URIs of services associated with the standard ID.
     * @param standardID base standard ID URI
     * @return set of service URIs
     */
    public Set<URI> getServiceURIs(URI standardID) {
        Set<URI> resourceIdentifiers = authorityMap.get(standardID);
        if ((resourceIdentifiers == null)) {
            throw new NoSuchElementException("not found: " + standardID);
        }
        return resourceIdentifiers;
    }
}
