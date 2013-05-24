package com.agimatec.commons.config;

import java.math.BigDecimal;

/**
 * Title:
 * Description:
 * Company:
 *
 * @author
 */

public class DecimalNode extends Node {
    protected BigDecimal value;

    public DecimalNode() {
    }

    public Object getObjectValue() {
        return value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(final BigDecimal aValue) {
        value = aValue;
    }
}