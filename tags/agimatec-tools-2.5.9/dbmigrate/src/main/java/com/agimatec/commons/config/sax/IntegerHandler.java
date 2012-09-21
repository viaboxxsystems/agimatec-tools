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
class IntegerHandler extends PrimNodeHandler {
    public IntegerHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return Integer.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final String value = getValue(attr);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new SAXException("not an int: " + value, ex);
        }
    }
}