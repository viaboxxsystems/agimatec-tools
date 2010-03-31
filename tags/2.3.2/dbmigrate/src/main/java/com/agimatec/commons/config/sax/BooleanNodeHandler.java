package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.BooleanNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Roman Stumm
 */

class BooleanNodeHandler extends NodeHandler {

    public BooleanNodeHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return BooleanNode.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final BooleanNode node = new BooleanNode();
        setName(node, attr);
        setValue(node, getValue(attr));
        return node;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length)
            throws SAXException {
        final BooleanNode node = (BooleanNode) docHandler.getCurrentNode();
        setValue(node, new String(chars, offset, length));
    }


    private void setValue(final BooleanNode node, final String stringValue) {
        node.setValue(("true".equalsIgnoreCase(stringValue) ||
                "yes".equalsIgnoreCase(stringValue)));
    }
}
