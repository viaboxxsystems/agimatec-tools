package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.IntNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */
class IntNodeHandler extends NodeHandler {
    public IntNodeHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return IntNode.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final IntNode node = new IntNode();
        setName(node, attr);
        final String value = getValue(attr);
        if (value != null) {
            setValue(node, value);
        }
        return node;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length)
            throws SAXException {
        final IntNode node = (IntNode) docHandler.getCurrentNode();
        setValue(node, new String(chars, offset, length));
    }

    private void setValue(final IntNode node, final String stringValue)
            throws SAXException {
        try {
            node.setValue(Integer.parseInt(stringValue));
        } catch (NumberFormatException ex) {
            throw new SAXException("not an int: " + stringValue, ex);
        }
    }
}