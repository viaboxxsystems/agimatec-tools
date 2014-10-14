package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.ColumnDescription;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 03.07.2007 <br/>
 * Time: 16:23:30 <br/>
 */
public class Ejb3Attribute extends Ejb3Prototype {
    private static final Map<String, String> typeNames = new HashMap();
    private static final Map<String, String> typeNamesNullable = new HashMap();

    /**
     * mapping von DBMS-Typen auf Property-Typen in der Ejb3-Java-Klasse
     */
    static {
        // allgemein oder wenn NOT_NULL
        typeNames.put("INTEGER", "int");
        typeNames.put("SMALLINT", "int");
        typeNames.put("BIGINT", "long");
        typeNames.put("VARCHAR", "String");
        typeNames.put("CHARACTER VARIYING", "String");
        typeNames.put("CHARACTER", "String");
        typeNames.put("TIMESTAMP", "java.sql.Timestamp");
        typeNames.put("DATE", "java.sql.Date");
        typeNames.put("TIME", "java.sql.Time");
        typeNames.put("BOOLEAN", "boolean");
        typeNames.put("TEXT", "String");
        typeNames.put("BYTEA", "byte[]");

        // andere, wenn NULLABLE
        typeNamesNullable.put("INTEGER", "Integer");
        typeNamesNullable.put("SMALLINT", "Integer");
        typeNamesNullable.put("BIGINT", "Long");
        typeNamesNullable.put("BOOLEAN", "Boolean");
    }

    private final ColumnDescription column;
    private String javaType;
    private String attributeName;

    public Ejb3Attribute(ColumnDescription column) {
        this.column = column;
    }

    private String toJavaType(ColumnDescription column) {
        String jtype = null;
        if (column.isNullable()) {
            jtype = typeNamesNullable.get(column.getTypeName());
        }
        if (jtype == null) {
            jtype = typeNames.get(column.getTypeName());
        }
        if (jtype == null) return column.getTypeName();
        return jtype;
    }

    public ColumnDescription getColumn() {
        return column;
    }

    public void generate() {
        attributeName = toProperAttributeName(column.getColumnName());
        if (isEnumType()) {
            javaType = toProperEntityName(attributeName);
        } else {
            javaType = toJavaType(column);
        }
    }

    public String getJavaType() {
        return javaType;
    }

    public boolean isEnumType() {
        return column.getComment() != null &&
                column.getComment().toLowerCase().indexOf("enum") > -1;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getGetter() {
        if ("boolean".equals(javaType)) {
            return "is" + StringUtils.capitalize(getAttributeName());
        } else {
            return "get" + StringUtils.capitalize(getAttributeName());
        }
    }

    public String getSetter() {
        return "set" + StringUtils.capitalize(getAttributeName());
    }

    public String toString() {
        return attributeName;
    }
}
