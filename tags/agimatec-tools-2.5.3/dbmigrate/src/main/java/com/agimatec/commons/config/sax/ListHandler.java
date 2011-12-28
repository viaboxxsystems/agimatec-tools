package com.agimatec.commons.config.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */
class ListHandler extends NodeHandler {
    protected Class instanceClass;

    public ListHandler(final String aTag, final Class theInstanceClass) {
        super(aTag);
        instanceClass = theInstanceClass;
    }

    protected Class getInstanceClass() {
        return instanceClass;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        try {
            return (List) instanceClass.newInstance();
        } catch (Exception ex) {
            throw new SAXException(ex);
        }
    }

    protected void acceptNode(final ConfigContentHandler docHandler, final Object aNode, final Attributes attr)
            throws SAXException {
        ((List) docHandler.getCurrentNode()).add(aNode);
    }

    protected void writeConfig(final ConfigWriter writer, final Object obj, final Object parent, final PrintWriter pw, int indent)
            throws SAXException {
        final List node = (List) obj;
        pw.write('<');
        pw.write(tag());
        writeParentName(pw, parent);
        pw.write('>');

        final Iterator iter = node.iterator();
        indent++;
        while (iter.hasNext()) {
            final Object each = iter.next();
            pw.write('\n');
            printIndent(pw, indent);
            writer.writeNode(each, pw, indent, node);
        }
        pw.write('\n');
        printIndent(pw, indent - 1);
        pw.write("</");
        pw.write(tag());
        pw.write('>');
    }
}