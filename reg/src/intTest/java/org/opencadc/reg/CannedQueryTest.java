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
************************************************************************
*/

package org.opencadc.reg;

import ca.nrc.cadc.auth.AuthMethod;
import ca.nrc.cadc.net.HttpGet;
import ca.nrc.cadc.reg.Standards;
import ca.nrc.cadc.reg.client.RegistryClient;
import ca.nrc.cadc.util.Log4jInit;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jeevesh
 */
public class CannedQueryTest {
    private static final Logger log = Logger.getLogger(CannedQueryTest.class);
    private static final URI TEST_APP_RESOURCE_ID = URI.create("ivo://opencadc.org/reg");

    static {
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
        Log4jInit.setLevel("org.opencadc.reg", Level.INFO);
    }

    public CannedQueryTest() {
    }
    
    @Test
    public void testApplicationsEndpoint() {
        try {
            RegistryClient applicationsRC = new RegistryClient();
            URL capURL = applicationsRC.getServiceURL(TEST_APP_RESOURCE_ID, Standards.VOSI_CAPABILITIES, AuthMethod.ANON);
            Assert.assertNotNull(capURL);
            
            // HACK: unregistered
            URL target = new URL(capURL.toExternalForm().replace("/capabilities", "/applications"));
            log.info("applications URL endpoint: " + target);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(target, bos);
            get.run();
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/plain", get.getContentType());
            
            log.info("content-length = " + get.getContentLength());
            log.info("content:\n" + bos.toString());
        } catch (Exception ex) {
            throw new RuntimeException("BUG: failed to find and access test applications endpoint " + TEST_APP_RESOURCE_ID, ex);
        }
    }
    
    @Test
    public void testResourceCapsEndpoint() {
        try {
            RegistryClient applicationsRC = new RegistryClient();
            URL capURL = applicationsRC.getServiceURL(TEST_APP_RESOURCE_ID, Standards.VOSI_CAPABILITIES, AuthMethod.ANON);
            Assert.assertNotNull(capURL);
            
            // HACK: unregistered
            URL url = new URL(capURL.toExternalForm().replace("/capabilities", "/resource-caps"));
            log.info("resource-caps URL endpoint: " + url);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpGet get = new HttpGet(url, bos);
            get.run();
            log.info("result: " + get.getResponseCode() + " " + get.getThrowable());
            Assert.assertNull("throwable", get.getThrowable());
            Assert.assertEquals(200, get.getResponseCode());
            Assert.assertEquals("text/plain", get.getContentType());

            log.info("content-length = " + get.getContentLength());
            log.info("content:\n" + bos.toString());
        } catch (Exception ex) {
            throw new RuntimeException("BUG: failed to find and access test applications endpoint " + TEST_APP_RESOURCE_ID, ex);
        }
    }

}
