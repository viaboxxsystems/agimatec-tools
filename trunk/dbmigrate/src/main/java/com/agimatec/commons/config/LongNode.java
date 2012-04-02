package com.agimatec.commons.config;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author
 */

public class LongNode extends Node {
    protected long value;

    public LongNode() {
    }

    public Object getObjectValue() {
        return value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(final long aValue) {
        value = aValue;
    }
}