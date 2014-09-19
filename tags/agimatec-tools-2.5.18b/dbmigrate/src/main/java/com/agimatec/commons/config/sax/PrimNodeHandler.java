package com.agimatec.commons.config.sax;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.SAXException;

import java.io.PrintWriter;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */
abstract class PrimNodeHandler extends NodeHandler {
    public PrimNodeHandler(final String aTag) {
        super(aTag);
    }

    protected void writeConfig(final ConfigWriter writer, final Object node, final Object parent, final PrintWriter pw, final int indent)
            throws SAXException {
        pw.write("<");
        pw.write(tag());
        writeParentName(pw, parent);
        if (node != null) {
            pw.write(" value=\"");
            pw.write(StringEscapeUtils.escapeXml(node.toString()));
            pw.write("\"");
        }
        pw.write("/>");
    }
}