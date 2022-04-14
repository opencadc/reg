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
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.vosi;

import ca.nrc.cadc.date.DateUtil;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * @author zhangsa
 */
public class Availability {

    private final boolean available;
    
    public String note;
    public Date upSince;
    public Date downAt;
    public Date backAt;
    
    public String clientIP;

    public Availability(boolean available) {
        this.available = available;
    }
    
    public Availability(boolean available, String note) {
        this.available = available;
        this.note = note;
    }

    public boolean isAvailable() {
        return available;
    }
    
    @Deprecated
    public Availability(AvailabilityStatus status) {
        super();
        if (status == null) {
            throw new IllegalArgumentException("Availability Status is null.");
        }
        this.available = status.isAvailable();
        this.note = status.getNote();
        this.upSince = status.getUpSince();
        this.downAt = status.getDownAt();
        this.backAt = status.getBackAt();
    }

    @Deprecated
    public void setClientIP(final String clientIP) {
        this.clientIP = clientIP;
    }

    /**
     * partial output support. TODO: implement AvailabilityWriter.
     * 
     * @param avail
     * @return JDOM Document
     * @deprecated
     */
    @Deprecated
    public static Document toXmlDocument(Availability avail) {
        DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);
        Namespace vosi = Namespace.getNamespace("vosi", VOSI.AVAILABILITY_NS_URI);

        Element eleAvailability = new Element("availability", vosi);

        Util.addChild(eleAvailability, vosi, "available", Boolean.toString(avail.isAvailable()));
        if (avail.upSince != null) {
            Util.addChild(eleAvailability, vosi, "upSince", df.format(avail.upSince));
        }
        if (avail.downAt != null) {
            Util.addChild(eleAvailability, vosi, "downAt", df.format(avail.downAt));
        }
        if (avail.backAt != null) {
            Util.addChild(eleAvailability, vosi, "backAt", df.format(avail.backAt));
        }
        if (avail.note != null) {
            Util.addChild(eleAvailability, vosi, "note", avail.note);
        }
        if (avail.clientIP != null) {
            eleAvailability.addContent(new Comment(String.format("<clientip>%s</clientip>", avail.clientIP)));
        }

        Document document = new Document();
        document.addContent(eleAvailability);

        return document;
    }

    /**
     * Partial input support. TODO: implement AvailabilityReader.
     * 
     * @param doc JDOM Document
     * @return availability object
     * @throws ParseException
     * @deprecated
     */
    @Deprecated
    public static Availability fromXmlDocument(Document doc) throws ParseException {
        Namespace vosi = Namespace.getNamespace("vosi", VOSI.AVAILABILITY_NS_URI);
        Element availability = doc.getRootElement();
        if (!availability.getName().equals("availability")) {
            throw new IllegalArgumentException("missing root element 'availability'");
        }

        Element elemAvailable = availability.getChild("available", vosi);
        if (elemAvailable == null) {
            throw new IllegalArgumentException("missing element 'available'");
        }
        boolean available = elemAvailable.getText().equalsIgnoreCase("true");
        Availability ret = new Availability(available);
        
        DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);

        Element elemUpSince = availability.getChild("upSince", vosi);
        ret.upSince = safeParseDate(elemUpSince, df);
        
        Element elemDownAt = availability.getChild("downAt", vosi);
        ret.downAt = safeParseDate(elemDownAt, df);
        
        Element elemBackAt = availability.getChild("backAt", vosi);
        ret.backAt = safeParseDate(elemBackAt, df);
        
        Element elemNote = availability.getChild("note", vosi);
        ret.note = elemNote.getText();
        
        for (Content c : availability.getContent()) {
            if (c instanceof Comment) {
                Comment com = (Comment) c;
                String s = com.getText();
                if (s.startsWith("<clientip>") && s.endsWith("/clientip>")) {
                    ret.clientIP = s.substring(10, s.length() - 11);
                }
            }
        }

        return ret;
    }
    
    private static Date safeParseDate(Element e, DateFormat df) throws ParseException {
        if (e == null || e.getTextTrim().isEmpty()) {
            return null;
        }
        return df.parse(e.getTextTrim());
    }

    @Deprecated
    public AvailabilityStatus getStatus() {
        return new AvailabilityStatus(available, upSince, downAt, backAt, note);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Availability[");
        sb.append("available=").append(available);
        if (upSince != null) {
            sb.append(",upSince=").append(upSince);
        }
        if (downAt != null) {
            sb.append(",downAt=").append(downAt);
        }
        if (backAt != null) {
            sb.append(",backAt=").append(backAt);
        }
        if (note != null) {
            sb.append(",note=").append(note);
        }
        if (clientIP != null) {
            sb.append(",clientip=").append(clientIP);
        }
        sb.append("]");
        return sb.toString();
    }

}
