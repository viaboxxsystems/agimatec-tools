package com.agimatec.commons.config;

/**
 * Title:
 * Description:
 * Company:
 *
 */

public class IntNode extends Node {
    protected int value;

    public IntNode() {
    }

    public Object getObjectValue() {
        return value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int aValue) {
        value = aValue;
    }
}