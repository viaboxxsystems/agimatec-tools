package com.agimatec.commons.config.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author Roman Stumm
 */

class LongHandler extends PrimNodeHandler {
    public LongHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return Long.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final String value = getValue(attr);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new SAXException("not a long: " + value, ex);
        }
    }
}