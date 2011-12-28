package com.agimatec.commons.config;


/**
 * Title:
 * Description:
 * Company:
 *
 * @author
 */

public class TextNode extends Node {
    protected String value;

    public TextNode() {
    }

    public Object getObjectValue() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String aValue) {
        value = aValue;
    }
}