package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.Node;
import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.PrintWriter;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */

public abstract class NodeHandler {
    private final String tag;

    public NodeHandler(final String aTag) {
        this.tag = aTag;
    }

    protected void acceptNode(final ConfigContentHandler docHandler, final Object aNode, final Attributes attr)
            throws SAXException {
        throw new SAXException(tag() + " cannot have subelements");
    }

    protected String tag() {
        return tag;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length)
            throws SAXException {
        // do nothing
    }

    /**
     * append a new string to on oldText, if oldText is != null. otherwise return the new string
     *
     * @param oldText - the previous text value
     * @param chars
     * @param offset
     * @param length
     * @return
     */
    protected String appendCharacters(String oldText, char[] chars, int offset, int length) {
        final String newText = new String(chars, offset, length);
        if (oldText == null) {
            return newText;
        } else {
            return oldText + newText;
        }
    }

    protected abstract Object startNode(Attributes attr) throws SAXException;

    protected abstract Class getInstanceClass();

    protected void setName(final Node node, final Attributes attr) {
        node.setName(getName(attr));
    }

    protected String getValue(final Attributes attr) {
        return attr.getValue("value");
    }

    protected String getName(final Attributes attr) {
        return attr.getValue("name");
    }

    protected void writeConfig(final ConfigWriter writer, final Object obj, final Object parent, final PrintWriter pw, final int indent)
            throws SAXException {
        final Node node = (Node) obj;
        pw.write("<");
        pw.write(tag());
        if (node.getName() != null) {
            pw.write(" name=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getName()));
            pw.write("\"");
        }
        if (node.getObjectValue() != null) {
            pw.write(" value=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getObjectValue().toString()));
            pw.write("\"");
        }
        pw.write("/>");
    }

    /**
     * util method for toString() xml conversion.
     * this method makes the indent of the xml lines.
     */
    protected final void printIndent(final PrintWriter pw, final int indent) {
        for (int i = 0; i < indent; i++) {
            pw.write("  ");
        }
    }

    protected void writeParentName(final PrintWriter pw, final Object parent) {
        if (parent != null && parent instanceof String) {
            pw.write(" name=\"");
            pw.write(StringEscapeUtils.escapeXml((String) parent));
            pw.write("\"");
        }
    }
}
