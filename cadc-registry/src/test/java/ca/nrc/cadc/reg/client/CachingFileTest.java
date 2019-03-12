/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2017.                            (c) 2017.
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

import java.io.File;
import java.net.URL;
import java.security.MessageDigest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.cadc.util.HexUtil;
import ca.nrc.cadc.util.Log4jInit;
import java.nio.file.Files;

public class CachingFileTest
{

    private static Logger log = Logger.getLogger(CachingFileTest.class);
    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.reg", Level.INFO);
    }

    @Test
    public void testCachingFile()
    {
        try
        {
            // make sure the cache is on alocal filesystem or small NFS time diff causes it to fail
            File file = new File(System.getProperty("user.dir") + "/build/tmp/" + CachingFileTest.class.getSimpleName()+".cache");
            // web site content changes on each request
            URL url = new URL("https://www.uuidgenerator.net/");
            // cache expires in 10 seconds
            CachingFile cachingFile = new CachingFile(file, url, 10);
            String md51 = getMd5Sum(cachingFile.getContent());

            // wait 4 seconds
            Thread.sleep(4000);
            String md52 = getMd5Sum(cachingFile.getContent());
            Assert.assertEquals(md51, md52);

            // wait 7 seconds
            Thread.sleep(7000);
            String md53 = getMd5Sum(cachingFile.getContent());
            Assert.assertNotEquals(md51, md53);
        }
        catch (Throwable t)
        {
            log.error("unexpected throwable", t);
            Assert.fail("unexpected throwable: " + t.getMessage());
        }
    }

    private String getMd5Sum(String s) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(s.getBytes());
        return HexUtil.toHex(bytes);
    }

    @Test
    public void testNullFile()
    {
        try
        {
            URL url = new URL("http://www.canfar.net");
            new CachingFile(null, url);
            Assert.fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertTrue(e.getMessage().contains("localCache"));
            Assert.assertTrue(e.getMessage().contains("required"));
        }
        catch (Throwable t)
        {
            log.error("unexpected throwable", t);
            Assert.fail("unexpected throwable: " + t.getMessage());
        }
    }

    @Test
    public void testNullURL()
    {
        try
        {
            File file = new File(System.getProperty("user.dir") + "/build/tmp/cft/foo.properties");
            new CachingFile(file, null);
            Assert.fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertTrue(e.getMessage().contains("remoteSource"));
            Assert.assertTrue(e.getMessage().contains("required"));
        }
        catch (Throwable t)
        {
            log.error("unexpected throwable", t);
            Assert.fail("unexpected throwable: " + t.getMessage());
        }
    }

    @Test
    public void testReplaceFileWithDir()
    {
        try
        {
            File file = new File(System.getProperty("user.dir") + "/build/tmp/cft-replace");
            Files.deleteIfExists(file.toPath());
            Files.createDirectories(file.getParentFile().toPath());
            Assert.assertTrue(file.getParentFile().exists());
            Assert.assertTrue(file.getParentFile().isDirectory());
            
            file.createNewFile();
            Assert.assertTrue(file.exists());
            Assert.assertTrue(file.isFile());
            
            File cff = new File(file.getAbsoluteFile(), "TADA");
            
            URL url = new URL("http://www.canfar.net");
            CachingFile cf = new CachingFile(cff, url);
            
            Assert.assertTrue(file.isDirectory());
        }
        catch (Exception ex)
        {
            log.error("unexpected exception", ex);
            Assert.fail("unexpected exception: " + ex);
        }
    }

    @Test
    public void testNonHttpScheme()
    {
        try
        {
            File file = new File(System.getProperty("user.dir") + "/build/tmp/cft/foo.properties");
            URL url = new URL("ftp://www.canfar.net");
            new CachingFile(file, url);
            Assert.fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            log.debug("IllegalArgument: " + e.getMessage());
            Assert.assertTrue(e.getMessage().contains("remoteSource"));
            Assert.assertTrue(e.getMessage().contains("scheme"));
        }
        catch (Throwable t)
        {
            log.error("unexpected throwable", t);
            Assert.fail("unexpected throwable: " + t.getMessage());
        }
    }

}
