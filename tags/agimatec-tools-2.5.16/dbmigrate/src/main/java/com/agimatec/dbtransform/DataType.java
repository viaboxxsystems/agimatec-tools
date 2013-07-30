package com.agimatec.dbtransform;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 05.06.2007 <br/>
 * Time: 16:41:43 <br/>
 * Copyright: Agimatec GmbH
 */
@XStreamAlias("dataType")
public class DataType implements Cloneable {
    private Integer precision;
    private Boolean precisionEnabled;
    private Integer scale;
    private String typeName; // typ als String, z.B. BIGINT

    public DataType() {
    }

    public DataType deepCopy()
    {
        try {
            return (DataType) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public DataType(String typeName) {
        this.typeName = typeName;
    }

    public DataType(String typeName, Integer precision) {
        this(typeName, precision, null);
    }


    public DataType(String typeName, Integer precision, Integer scale) {
        this.typeName = typeName;
        this.precision = precision;
        this.scale = scale;
        precisionEnabled = true;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Boolean isPrecisionEnabled() {
        return precisionEnabled;
    }

    public void setPrecisionEnabled(Boolean precisionEnabled) {
        this.precisionEnabled = precisionEnabled;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean match(DataType dt) {
        if (dt.getTypeName().equalsIgnoreCase(typeName)) {
            if (precision == null || dt.getPrecision() == null ||
                    precision.equals(dt.getPrecision())) {
                if (scale == null || dt.getScale() == null ||
                        scale.equals(dt.getScale())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        return "DataType{" + "typeName='" + typeName + '\'' + ", precision=" + precision +
                ", scale=" + scale + '}';
    }
}
