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
*  $Revision: 5 $
*
************************************************************************
 */

package ca.nrc.cadc.reg;

import ca.nrc.cadc.util.Log4jInit;
import java.net.URI;
import java.net.URL;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author yeunga
 */
public class InterfaceTest {

    private static final Logger log = Logger.getLogger(InterfaceTest.class);

    private URI ITYPE = Standards.INTERFACE_PARAM_HTTP;
    private String ACCESS_URL = "https://example.net/tap/availability";

    static {
        Log4jInit.setLevel("ca.nrc.cadc.vosi", Level.INFO);
    }

    public InterfaceTest() {
    }

    @Test
    public void testNullType() {
        try {
            Interface i = new Interface(null, new AccessURL(new URL(ACCESS_URL)));
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable t) {
            Assert.fail("unexpected t: " + t);
        }
    }

    @Test
    public void testNullAccessURL() {
        try {
            new Interface(ITYPE, null);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        } catch (Throwable t) {
            Assert.fail("unexpected t: " + t);
        }
    }

    @Test
    public void testConstruction() {
        try {
            Interface intf = new Interface(ITYPE, new AccessURL(new URL(ACCESS_URL)));

            URI type = intf.getType();
            Assert.assertEquals(ITYPE, type);

            AccessURL accessURL = intf.getAccessURL();
            Assert.assertNotNull("accessURL should not be null", accessURL);
            Assert.assertEquals("accessURL is corrupted", ACCESS_URL, accessURL.getURL().toString());
        } catch (Throwable t) {
            log.error("unexpected exception", t);
            Assert.fail("unexpected exception: " + t);
        }
    }

    @Test
    public void testSecurityMethods() {
        try {
            Interface intf = new Interface(ITYPE, new AccessURL(new URL(ACCESS_URL)));
            intf.getSecurityMethods().add(Standards.SECURITY_METHOD_ANON);
            intf.getSecurityMethods().add(Standards.SECURITY_METHOD_CERT);

            Assert.assertEquals(2, intf.getSecurityMethods().size());

            URI anon = intf.getSecurityMethods().get(0);
            Assert.assertEquals(Standards.SECURITY_METHOD_ANON, anon);
            
            URI cert = intf.getSecurityMethods().get(1);
            Assert.assertEquals(Standards.SECURITY_METHOD_CERT, cert);
        } catch (Throwable t) {
            log.error("unexpected exception", t);
            Assert.fail("unexpected exception: " + t);
        }
    }
}
