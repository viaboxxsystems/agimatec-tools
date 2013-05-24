package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.MapNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */

class MapNodeHandler extends NodeHandler {
    protected Class instanceClass;

    public MapNodeHandler(final String aTag, final Class theInstanceClass) {
        super(aTag);
        instanceClass = theInstanceClass;
    }

    protected Class getInstanceClass() {
        return instanceClass;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        try {
            final MapNode node = (MapNode) instanceClass.newInstance();
            setName(node, attr);
            return node;
        } catch (Exception ex) {
            throw new SAXException(ex);
        }
    }

    protected void acceptNode(final ConfigContentHandler docHandler, final Object aNode, final Attributes attr)
            throws SAXException {
        final String nam = getName(attr);
        if (nam == null) throwNameRequired(aNode);
        ((MapNode) docHandler.getCurrentNode()).getMap().put(nam, aNode);
    }

    protected void throwNameRequired(final Object aNode) throws SAXException {
        throw new SAXException(
                "name required when " + aNode + " is element of a " + tag());
    }

    protected void writeConfig(final ConfigWriter writer, final Object obj, final Object parent, final PrintWriter pw, int indent)
            throws SAXException {
        final MapNode node = (MapNode) obj;
        pw.write('<');
        pw.write(tag());
        if (node.getName() != null) {
            pw.write(" name=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getName()));
            pw.write("\"");
        }
        pw.write('>');

        ArrayList myArrayList = new ArrayList();
        myArrayList.addAll(node.getMap().keySet());
        Collections.sort(myArrayList);

        final Iterator iter = myArrayList.iterator();
        indent++;
        while (iter.hasNext()) {
            final Object myKey = iter.next();
            final Object myValue = node.get(myKey);
            //final Map.Entry each = (Map.Entry)iter.next();
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
