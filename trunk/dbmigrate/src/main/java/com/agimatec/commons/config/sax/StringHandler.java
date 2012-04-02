package com.agimatec.commons.config.sax;

import org.xml.sax.Attributes;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */
class StringHandler extends PrimNodeHandler {
    public StringHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return String.class;
    }

    protected Object startNode(final Attributes attr) {
        return getValue(attr);
    }
}