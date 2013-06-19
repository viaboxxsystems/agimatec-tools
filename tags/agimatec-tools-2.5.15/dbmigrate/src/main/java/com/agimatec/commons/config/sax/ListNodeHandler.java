package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.ListNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */
class ListNodeHandler extends NodeHandler {
    public ListNodeHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return ListNode.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final ListNode node = new ListNode();
        setName(node, attr);
        return node;
    }

    protected void acceptNode(final ConfigContentHandler docHandler, final Object aNode, final Attributes attr)
            throws SAXException {
        ((ListNode) docHandler.getCurrentNode()).getList().add(aNode);
    }

    protected void writeConfig(final ConfigWriter writer, final Object obj, final Object parent, final PrintWriter pw, int indent)
            throws SAXException {
        final ListNode node = (ListNode) obj;
        pw.write('<');
        pw.write(tag());
        if (node.getName() != null) {
            pw.write(" name=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getName()));
            pw.write("\"");
        }
        pw.write('>');

        final Iterator iter = node.getList().iterator();
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