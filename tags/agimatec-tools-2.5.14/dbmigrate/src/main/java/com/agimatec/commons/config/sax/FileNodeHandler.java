package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.config.FileNode;
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
public class FileNodeHandler extends NodeHandler {
    protected ConfigManager myConfigManager;

    public FileNodeHandler(final ConfigManager aConfigManager, final String aTag) {
        super(aTag);
        myConfigManager = aConfigManager;
    }

    protected Class getInstanceClass() {
        return FileNode.class;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length) {
        final FileNode node = (FileNode) docHandler.getCurrentNode();
        node.setFile(appendCharacters(node.getFile(), chars, offset, length));
    }

    protected Object startNode(final Attributes attr) {
        final FileNode node = new FileNode(myConfigManager);
        setName(node, attr);
        node.setDir(attr.getValue("dir"));
        node.setFile(attr.getValue("file"));
        final String value = attr.getValue("relative");
        boolean theRelative = true;
        if (value != null) theRelative = "true".equalsIgnoreCase(value) ||
                "yes".equalsIgnoreCase(
                        value); // used like this (with if) so true is default value
        node.setRelative(theRelative);
        return node;
    }

    protected void writeConfig(final ConfigWriter writer, final Object obj, final Object parent, final PrintWriter pw, final int indent)
            throws SAXException {
        final FileNode node = (FileNode) obj;
        pw.write("<");
        pw.write(tag());
        if (node.getName() != null) {
            pw.write(" name=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getName()));
            pw.write("\"");
        }
        if (node.getDir() != null) {
            pw.write(" dir=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getDir()));
            pw.write("\"");
        }

        if (node.getFile() != null) {
            pw.write(" file=\"");
            pw.write(StringEscapeUtils.escapeXml(node.getFile()));
            pw.write("\"");
        }

        String relative = (node.getRelative()) ? "true" : "false";
        pw.write(" relative=\"");
        pw.write(relative);
        pw.write("\"");

        pw.write("/>");
    }
}
