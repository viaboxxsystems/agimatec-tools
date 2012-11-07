package com.agimatec.commons.config;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author
 */

public class DoubleNode extends Node {
    protected double value;

    public DoubleNode() {
    }

    public Object getObjectValue() {
        return value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double aValue) {
        value = aValue;
    }
}