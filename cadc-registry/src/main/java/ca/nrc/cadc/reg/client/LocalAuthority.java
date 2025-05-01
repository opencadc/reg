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

import ca.nrc.cadc.util.InvalidConfigException;
import ca.nrc.cadc.util.MultiValuedProperties;
import ca.nrc.cadc.util.PropertiesReader;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 * Uses the library configuration file (cadc-registry.properties) to find the
 * local service that provides the specified API (via standardID).
 * 
 * @author pdowler
 */
public class LocalAuthority {
    private static final Logger log = Logger.getLogger(LocalAuthority.class);

    private static final Set<URI> EMPTY_SET = Collections.unmodifiableSet(Collections.EMPTY_SET);
    
    private final Map<URI, Set<URI>> authorityMap = new TreeMap<>();

    public LocalAuthority() {
        PropertiesReader propReader = new PropertiesReader(RegistryClient.CONFIG_FILE);
        MultiValuedProperties mvp = propReader.getAllProperties();
        
        for (String std : mvp.keySet()) {
            List<String> values = mvp.getProperty(std);
            Set<URI> vals = new TreeSet<>(); // TreeSet is sorted so get-first will be predictable
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
     * @throws NoSuchElementException if configuration for this standardID not found
     * @deprecated deprecated in favour of getServiceURIs method
     */
    @Deprecated
    public URI getServiceURI(String baseStandardID) {
        Set<URI> resourceIdentifiers = lookup(URI.create(baseStandardID));
        if (resourceIdentifiers.isEmpty()) {
            throw new NoSuchElementException("not found: " + baseStandardID);
        }
        if (resourceIdentifiers.size() > 1) {
            throw new NoSuchElementException("Multiple service URIs found for " + baseStandardID);
        }
        return resourceIdentifiers.iterator().next();
    }
    
    /**
     * Convenience: Get a single (first) resourceID that provides the specified standardID
     * and do not fail if configuration includes multiple values.
     * 
     * @param standardID the requested API feature
     * @return a resourceID that implements the feature or null if no configured provider
     */
    public URI getResourceID(URI standardID) {
        return getResourceID(standardID, true);
    }
    
    /**
     * Get a single (first) resourceID that provides the specified standardID.
     * 
     * @param standardID the requested API feature
     * @param failOnMultiple treat multiple values as a configuration error
     * @return a resourceID that implements the feature or null if no configured provider
     * @throws InvalidConfigException if failOnMultiple=true and configuration has multiple values
     */
    public URI getResourceID(URI standardID, boolean failOnMultiple) {
        Set<URI> s = lookup(standardID);
        if (s.isEmpty()) {
            return null;
        }
        if (failOnMultiple && s.size() > 1) {
            throw new InvalidConfigException("found multiple values for " + standardID);
        }
        return s.iterator().next();
    }

    /**
     * Returns the URIs of services associated with the standard ID.
     * @param standardID the requested API feature
     * @return all resourceID(s) that implement the feature
     * @throws NoSuchElementException if configuration for this standardID not found
     */
    public Set<URI> getResourceIDs(URI standardID) {
        return lookup(standardID);
    }
    
    private Set<URI> lookup(URI standardID) {
        Set<URI> ret = authorityMap.get(standardID);
        if (ret == null) {
            return EMPTY_SET;
        }
        return ret;
    }
}
