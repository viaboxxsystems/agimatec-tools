package com.agimatec.sql.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
@XStreamAlias("column")
public class ColumnDescription implements Serializable, Cloneable {
    private String comment;
    private String columnName;
    private boolean nullable; // false=mandatory field
    private int precision;
    private boolean precisionEnabled;
    private int scale;
    //private int sqlType;  // aus sql.Types
    private String typeName; // typ als String, z.B. BIGINT
    private String defaultValue;

    public ColumnDescription deepCopy() {
        try {
            return (ColumnDescription) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * comment on column
     *
     * @return
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String aComment) {
        comment = aComment;
    }

    public boolean isPrecisionEnabled() {
        return precisionEnabled;
    }

    public void setPrecisionEnabled(boolean aPrecision) {
        precisionEnabled = aPrecision;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int aPrecision) {
        precision = aPrecision;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String aColumnName) {
        columnName = aColumnName;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean aNullable) {
        nullable = aNullable;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String aTypeName) {
        typeName = aTypeName;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int aScale) {
        scale = aScale;
    }

    public String toString() {
        return getColumnName();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}

