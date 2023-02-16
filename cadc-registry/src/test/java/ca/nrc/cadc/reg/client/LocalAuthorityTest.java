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

package ca.nrc.cadc.reg.client;

import java.net.URI;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.util.Log4jInit;
import ca.nrc.cadc.util.PropertiesReader;

/**
 *
 * @author pdowler
 */
public class LocalAuthorityTest {

    private static final Logger log = Logger.getLogger(LocalAuthorityTest.class);

    // values from cadc-registry.properties
    private String SERVICE = Standards.CRED_DELEGATE_10.toASCIIString();
    private String SERVICE_URI = "ivo://cadc.nrc.ca/cred";
    
    private String OPENID = Standards.SECURITY_METHOD_OPENID.toASCIIString();
    private String OPENID_URL = "https://oidc.example.net/";
    
    // values from backwards compat LocalAuthority.properties
    private String SERVICE_URI_COMPAT = "ivo://cadc.nrc.ca/cred/compat";
    
    private String OPENID_URL_COMPAT = "https://oidc.example.net/compat";

    static {
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
    }

    public LocalAuthorityTest() {
    }

    private static String TEST_CONFIG_DIR = PropertiesReader.class.getName() + ".dir";

    @Test
    public void testNotFound() {
        try {
            System.setProperty(TEST_CONFIG_DIR, "src/test/resources");

            LocalAuthority loc = new LocalAuthority();

            try {
                URI uri = loc.getServiceURI("foo:bar");
                Assert.fail("expected NoSuchElementException, found: " + uri);
            } catch (NoSuchElementException expected) {
                log.debug("caught expected exception: " + expected);
            }

        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        } finally {
            System.clearProperty(TEST_CONFIG_DIR);
        }
    }

    @Test
    public void testFoundIVO() {
        try {
            System.setProperty(TEST_CONFIG_DIR, "src/test/resources");

            LocalAuthority loc = new LocalAuthority();
            URI uri = loc.getServiceURI(SERVICE);
            Assert.assertNotNull(uri);
            Assert.assertEquals(SERVICE_URI, uri.toASCIIString());
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        } finally {
            System.clearProperty(TEST_CONFIG_DIR);
        }
    }
    
    @Test
    public void testFoundURL() {
        try {
            System.setProperty(TEST_CONFIG_DIR, "src/test/resources");

            LocalAuthority loc = new LocalAuthority();
            URI uri = loc.getServiceURI(OPENID);
            Assert.assertNotNull(uri);
            Assert.assertEquals(OPENID_URL, uri.toASCIIString());
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        } finally {
            System.clearProperty(TEST_CONFIG_DIR);
        }
    }
    
    @Test
    public void testCompatFoundIVO() {
        try {
            System.setProperty(TEST_CONFIG_DIR, "src/test/resources/compat");

            LocalAuthority loc = new LocalAuthority();
            URI uri = loc.getServiceURI(SERVICE);
            Assert.assertNotNull(uri);
            Assert.assertEquals(SERVICE_URI_COMPAT, uri.toASCIIString());
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        } finally {
            System.clearProperty(TEST_CONFIG_DIR);
        }
    }
    
    @Test
    public void testCompatFoundURL() {
        try {
            System.setProperty(TEST_CONFIG_DIR, "src/test/resources/compat");

            LocalAuthority loc = new LocalAuthority();
            URI uri = loc.getServiceURI(OPENID);
            Assert.assertNotNull(uri);
            Assert.assertEquals(OPENID_URL_COMPAT, uri.toASCIIString());
        } catch (Exception unexpected) {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        } finally {
            System.clearProperty(TEST_CONFIG_DIR);
        }
    }
}
