package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.Config;
import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.config.MapNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Title:
 * Description:
 *
 * @author Roman Stumm
 */

public class ConfigContentHandler extends DefaultHandler {
    private Config root;
    private Stack nodeStack;
    private Stack nodeHandlerStack;
    protected Map elementMap;
    protected ConfigManager myConfigManager;

    public ConfigContentHandler(final ConfigManager aConfigManager) {
        myConfigManager = aConfigManager;
        elementMap = new HashMap();
        initElementMap();
    }

    // DocumentHandler interface methods
    public void characters(final char[] chars, final int offset, final int length)
            throws org.xml.sax.SAXException {
        getCurrentHandler().acceptCharacters(this, chars, offset, length);
    }

    public void endDocument() throws org.xml.sax.SAXException {
        if (!nodeStack.empty()) throw new SAXException("unexpected end of file");
    }

    public void endElement(final java.lang.String uri, final java.lang.String elementName, final java.lang.String qName)
            throws SAXException {
        try {
            //final Object aNode =
            nodeStack.pop();
            nodeHandlerStack.pop();
        } catch (EmptyStackException ex) {
            throw new SAXException(
                    "internal error - no parent element for " + elementName);
        }
    }

    public void startDocument() throws org.xml.sax.SAXException {
        root = null;
        nodeStack = new Stack();
        nodeHandlerStack = new Stack();
    }

    /**
     * add default NodeHandlers for config xml
     */
    protected final void initElementMap() {
        addHandler(new MapNodeHandler("config", Config.class));
        addHandler(new MapNodeHandler("map", MapNode.class));
        addHandler(new TextNodeHandler("text"));
        addHandler(new ListNodeHandler("list"));
        addHandler(new IntNodeHandler("int"));
        addHandler(new BooleanNodeHandler("boolean"));
        addHandler(new FileNodeHandler(myConfigManager, "file"));
        addHandler(new DecimalNodeHandler("decimal"));
        addHandler(new DoubleNodeHandler("double"));
        addHandler(new LongNodeHandler("long"));
        // Primitives
        addHandler(new MapHandler("HashMap", java.util.HashMap.class));
        addHandler(new MapHandler("Hashtable", java.util.Hashtable.class));
        addHandler(new StringHandler("String"));
        addHandler(new IntegerHandler("Integer"));
        addHandler(new LongHandler("Long"));
        addHandler(new BooleanHandler("Boolean"));
        addHandler(new BigDecimalHandler("BigDecimal"));
        addHandler(new DoubleHandler("Double"));
        addHandler(new ListHandler("ArrayList", java.util.ArrayList.class));
        addHandler(new ListHandler("Vector", java.util.Vector.class));
    }

    public void addHandler(final NodeHandler handler) {
        elementMap.put(handler.tag(), handler);
    }

    public NodeHandler removeHandler(final String tag) {
        return (NodeHandler) elementMap.remove(tag);
    }

    public void startElement(final java.lang.String namespaceURI, final java.lang.String elementName, final java.lang.String qName, final Attributes attr)
            throws SAXException {
        final NodeHandler nodeHdlr = (NodeHandler) elementMap.get(elementName);
        if (nodeHdlr != null) {
            final Object newNode;
            try {
                newNode = nodeHdlr.startNode(attr);
                acceptNode(nodeHdlr, newNode, attr);
            } catch (Exception ex) {
                throw new SAXException(ex);
            }
        } else throw new SAXException("unknown element " + elementName);
    }

    // other methods
    public Config getConfig() {
        return root;
    }

    protected void acceptNode(final NodeHandler handler, final Object aNode, final Attributes attr)
            throws SAXException {
        if (root == null) {
            if (aNode instanceof Config) {
                root = (Config) aNode;
            } else throw new SAXException(
                    "<config> expected, but received " + handler.tag());
        } else {
            getCurrentHandler().acceptNode(this, aNode, attr);
        }
        nodeStack.push(aNode);
        nodeHandlerStack.push(handler);
    }

    protected NodeHandler getCurrentHandler() {
        return (nodeHandlerStack.empty()) ? null : (NodeHandler) nodeHandlerStack.peek();
    }

    protected Object getCurrentNode() {
        return nodeStack.peek();
    }
}