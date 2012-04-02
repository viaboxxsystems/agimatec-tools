package com.agimatec.sql.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * <b>Description:</b>   Description of the definition of a Oracle Sequence <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 * <b>Creation Date:</b> 17.11.2007
 *
 * @author Roman Stumm
 */
@XStreamAlias("sequence")
public class SequenceDescription implements Serializable, Cloneable {
    private String sequenceName; // CREATE SEQUENCE SEQ_AR_SEQUENCE_NUMBER
    private int increment; // INCREMENT BY 1
    private long start; // START WITH  100
    private BigDecimal maxValue; // null == NOMAXVALUE
    private BigDecimal minValue; // null == NOMINVALUE
    private boolean cycle; // false == NOCYCLE
    private boolean order; // false == NOORDER
    private Integer cache; // CACHE 100, null == NOCACHE;

     public SequenceDescription deepCopy() {
        try {
            return (SequenceDescription) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String aSequenceName) {
        sequenceName = aSequenceName;
    }

    public void setNoMaxValue() {
        maxValue = null;
    }

    public void setNoMinValue() {
        minValue = null;
    }

    public void setNoCache() {
        cache = null;
    }

    public boolean isNoMaxValue() {
        return maxValue == null;
    }

    public boolean isNoMinValue() {
        return minValue == null;
    }

    public boolean isNoCache() {
        return cache == null || cache.intValue()==0;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int aIncrement) {
        increment = aIncrement;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal aMaxValue) {
        maxValue = aMaxValue;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal aMinValue) {
        minValue = aMinValue;
    }

    public boolean isCycle() {
        return cycle;
    }

    public void setCycle(boolean aCycle) {
        cycle = aCycle;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean aOrder) {
        order = aOrder;
    }

    public int getCache() {
        return cache == null ? 0 : cache.intValue();
    }

    public void setCache(int aCache) {
        cache = new Integer(aCache);
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String toString() {
        return getSequenceName();
    }
}
