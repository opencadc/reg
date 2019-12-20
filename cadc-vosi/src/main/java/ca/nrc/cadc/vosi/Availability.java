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
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * @author zhangsa
 */
public class Availability {

    private AvailabilityStatus status;
    private String clientIP;

    public Availability(AvailabilityStatus status) {
        super();
        if (status == null) {
            throw new IllegalArgumentException("Availability Status is null.");
        }
        this.status = status;
    }

    @Deprecated
    public Availability(Document xml) {
        super();
        if (xml == null) {
            throw new IllegalArgumentException("Document is null.");
        }
        try {
            status = fromXmlDocument(xml);
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid date format in XML", e);
        }
    }

    public void setClientIP(final String clientIP) {
        this.clientIP = clientIP;
    }

    @Deprecated
    public Document toXmlDocument() {
        DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);
        Namespace vosi = Namespace.getNamespace("vosi", VOSI.AVAILABILITY_NS_URI);

        Element eleAvailability = new Element("availability", vosi);

        Util.addChild(eleAvailability, vosi, "available", Boolean.toString(status.isAvailable()));
        if (status.getUpSince() != null) {
            Util.addChild(eleAvailability, vosi, "upSince", df.format(status.getUpSince()));
        }
        if (status.getDownAt() != null) {
            Util.addChild(eleAvailability, vosi, "downAt", df.format(status.getDownAt()));
        }
        if (status.getBackAt() != null) {
            Util.addChild(eleAvailability, vosi, "backAt", df.format(status.getBackAt()));
        }
        if (status.getNote() != null) {
            Util.addChild(eleAvailability, vosi, "note", status.getNote());
        }
        if (this.clientIP != null) {
            eleAvailability.addContent(new Comment(String.format("<clientip>%s</clientip>", this.clientIP)));
        }

        Document document = new Document();
        document.addContent(eleAvailability);

        return document;
    }

    @Deprecated
    public AvailabilityStatus fromXmlDocument(Document doc) throws ParseException {
        Namespace vosi = Namespace.getNamespace("vosi", VOSI.AVAILABILITY_NS_URI);
        Element availability = doc.getRootElement();
        if (!availability.getName().equals("availability")) {
            throw new IllegalArgumentException("missing root element 'availability'");
        }

        Element elemAvailable = availability.getChild("available", vosi);
        if (elemAvailable == null) {
            throw new IllegalArgumentException("missing element 'available'");
        }
        final boolean available = elemAvailable.getText().equalsIgnoreCase("true");

        DateFormat df = DateUtil.getDateFormat(DateUtil.IVOA_DATE_FORMAT, DateUtil.UTC);

        Element elemUpSince = availability.getChild("upSince", vosi);
        final Date upSince = safeParseDate(elemUpSince, df);
        
        Element elemDownAt = availability.getChild("downAt", vosi);
        final Date downAt = safeParseDate(elemDownAt, df);
        
        Element elemBackAt = availability.getChild("backAt", vosi);
        final Date backAt = safeParseDate(elemBackAt, df);
        
        Element elemNote = availability.getChild("note", vosi);
        String note = elemNote.getText();;

        return new AvailabilityStatus(available, upSince, downAt, backAt, note);
    }
    
    private Date safeParseDate(Element e, DateFormat df) throws ParseException {
        if (e == null || e.getTextTrim().isEmpty()) {
            return null;
        }
        return df.parse(e.getTextTrim());
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Availability[");
        sb.append("available=").append(status.isAvailable());
        if (status.getUpSince() != null) {
            sb.append(",upSince=").append(status.getUpSince());
        }
        if (status.getDownAt() != null) {
            sb.append(",downAt=").append(status.getDownAt());
        }
        if (status.getBackAt() != null) {
            sb.append(",backAt=").append(status.getBackAt());
        }
        if (status.getNote() != null) {
            sb.append(",note=").append(status.getNote());
        }
        sb.append("]");
        return sb.toString();
    }

}
