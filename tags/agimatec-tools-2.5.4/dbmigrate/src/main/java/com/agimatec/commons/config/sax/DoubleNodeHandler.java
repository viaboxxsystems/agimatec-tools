package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.DoubleNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */

class DoubleNodeHandler extends NodeHandler {
    public DoubleNodeHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return DoubleNode.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final DoubleNode node = new DoubleNode();
        setName(node, attr);
        final String value = getValue(attr);
        if (value != null) {
            setValue(node, value);
        }
        return node;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length)
            throws SAXException {
        final DoubleNode node = (DoubleNode) docHandler.getCurrentNode();
        setValue(node, new String(chars, offset, length));
    }

    private void setValue(final DoubleNode node, final String stringValue)
            throws SAXException {
        try {
            node.setValue(Double.parseDouble(stringValue));
        } catch (NumberFormatException ex) {
            throw new SAXException("not a double: " + stringValue, ex);
        }
    }
}