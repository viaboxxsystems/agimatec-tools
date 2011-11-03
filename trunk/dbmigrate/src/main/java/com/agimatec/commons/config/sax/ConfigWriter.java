package com.agimatec.commons.config.sax;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class to write configurations as XML to a PrintWriter.
 *
 * @author Roman Stumm
 */

public class ConfigWriter extends ConfigContentHandler {
    private Map typeHandlerMap;

    public ConfigWriter() {
        super(null);
        initReverseMap();
    }

    protected void initReverseMap() {
        final Iterator iter = elementMap.values().iterator();
        typeHandlerMap = new HashMap();
        while (iter.hasNext()) {
            final NodeHandler each = (NodeHandler) iter.next();
            typeHandlerMap.put(each.getInstanceClass(), each);
        }
    }

    public void writeConfig(final Object config, final PrintWriter pw)
            throws IOException, SAXException {
        writeNode(config, pw, 0, null);
    }

    protected void writeNode(final Object node, final PrintWriter pw, final int indent, final Object parentNode)
            throws SAXException {
        final NodeHandler handler;
        if (node == null) {  // null ==> <String/>
            handler = (NodeHandler) typeHandlerMap.get(String.class);
        } else {
            handler = (NodeHandler) typeHandlerMap.get(node.getClass());
        }
        if (handler == null) {
            pw.write("<!-- unknown type " + (node != null ? node.getClass() : ""));
            pw.write(String.valueOf(node));
            pw.write(" -->");
        } else {
            handler.writeConfig(this, node, parentNode, pw, indent);
        }
    }
}