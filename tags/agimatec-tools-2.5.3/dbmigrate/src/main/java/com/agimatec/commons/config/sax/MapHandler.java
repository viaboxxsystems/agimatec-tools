package com.agimatec.commons.config.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;


/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */

class MapHandler extends MapNodeHandler {

    public MapHandler(final String aTag, final Class theInstanceClass) {
        super(aTag, theInstanceClass);
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        try {
            return (Map) instanceClass.newInstance();
        } catch (Exception ex) {
            throw new SAXException(ex);
        }
    }

    protected void acceptNode(final ConfigContentHandler docHandler, final Object aNode, final Attributes attr)
            throws SAXException {
        final String nam = getName(attr);

        if (nam == null) throwNameRequired(aNode);
        ((Map) docHandler.getCurrentNode()).put(nam, aNode);
    }

    protected void writeConfig(final ConfigWriter writer, final Object obj, final Object parent, final PrintWriter pw, int indent)
            throws SAXException {
        final Map node = (Map) obj;
        pw.write('<');
        pw.write(tag());
        writeParentName(pw, parent);
        pw.write('>');

        ArrayList myArrayList = new ArrayList();
        myArrayList.addAll(node.keySet());
        Collections.sort(myArrayList);

        final Iterator iter = myArrayList.iterator();
        indent++;
        while (iter.hasNext()) {
            //final Map.Entry each = (Map.Entry)iter.next();
            final Object myKey = iter.next();
            final Object myValue = node.get(myKey);
            pw.write('\n');
            printIndent(pw, indent);
            //writer.writeNode(each.getValue(), pw, indent, each);
            writer.writeNode(myValue, pw, indent, myKey);
        }
        pw.write('\n');
        printIndent(pw, indent - 1);
        pw.write("</");
        pw.write(tag());
        pw.write('>');
    }
}