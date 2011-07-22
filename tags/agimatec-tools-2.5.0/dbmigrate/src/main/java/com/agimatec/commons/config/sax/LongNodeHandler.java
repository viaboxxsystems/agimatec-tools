package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.LongNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */
class LongNodeHandler extends NodeHandler {
    public LongNodeHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return LongNode.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final LongNode node = new LongNode();
        setName(node, attr);
        final String value = getValue(attr);
        if (value != null) {
            setValue(node, value);
        }
        return node;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length)
            throws SAXException {
        final LongNode node = (LongNode) docHandler.getCurrentNode();
        setValue(node, new String(chars, offset, length));
    }

    private void setValue(final LongNode node, final String stringValue)
            throws SAXException {
        try {
            node.setValue(Long.parseLong(stringValue));
        } catch (NumberFormatException ex) {
            throw new SAXException("not a long: " + stringValue, ex);
        }
    }
}
