package com.agimatec.commons.config.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 *
 * @author Roman Stumm
 */
class BooleanHandler extends PrimNodeHandler {

    public BooleanHandler(final String aTag) {
        super(aTag);
    }

    protected Class getInstanceClass() {
        return Boolean.class;
    }

    protected Object startNode(final Attributes attr) throws SAXException {
        final String value = getValue(attr);
        return Boolean.valueOf(value != null &&
                ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)));
    }
}