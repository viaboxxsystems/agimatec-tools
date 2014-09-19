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
class DoubleHandler extends PrimNodeHandler {
    public DoubleHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return Double.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final String value = getValue(attr);
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new SAXException("not a double: " + value, ex);
        }
    }
}