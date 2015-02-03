package com.agimatec.commons.config.sax;

import com.agimatec.commons.config.TextNode;
import org.xml.sax.Attributes;

/**
 * Title:
 * Description: parse text or String from Config
 * Company:
 *
 * @author Roman Stumm
 */
class TextNodeHandler extends NodeHandler {
    public TextNodeHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return TextNode.class;
    }

    protected void acceptCharacters(final ConfigContentHandler docHandler, final char[] chars, final int offset, final int length) {
        final TextNode txtNode = (TextNode) docHandler.getCurrentNode();
        txtNode.setValue(appendCharacters(txtNode.getValue(), chars, offset, length));
    }

    protected Object startNode(final Attributes attr) {
        final TextNode node = new TextNode();
        setName(node, attr);
        node.setValue(getValue(attr));
        return node;
    }
}
