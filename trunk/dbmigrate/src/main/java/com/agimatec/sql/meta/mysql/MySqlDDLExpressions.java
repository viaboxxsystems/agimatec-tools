package com.agimatec.sql.meta.mysql;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.ExtractExpr;

/**
 * <p>syntax expressions to parse MySql scripts</p>
 * User: roman.stumm@viaboxx.de<br>
 * Date: 26.03.13
 */
public class MySqlDDLExpressions extends DDLExpressions {
    public static final ExtractExpr[] expressions;

    static {
        expressions = compileExpressions("com/agimatec/sql/meta/mysql-ddl.xml");
    }

    @Override
    public ExtractExpr[] getExpressions() {
        return expressions;
    }

    /**
     * equalize type names
     */
    @Override
    public void equalizeColumn(ColumnDescription cd) {
        if (cd.getTypeName().equalsIgnoreCase("tinyint") && cd.getPrecision() == 1) {
            cd.setTypeName("BIT");
            cd.setPrecision(0);
        } else if (cd.getTypeName().equalsIgnoreCase("bool")) {
            cd.setTypeName("BIT");
            cd.setPrecision(0);
        }
        if(cd.getDefaultValue()!=null && !cd.getDefaultValue().equalsIgnoreCase("NULL") && cd.getTypeName().equalsIgnoreCase("TIMESTAMP")) {
            cd.setNullable(false);
        }
    }

    @Override
    public String strip(String value) {
        if (value == null) return null;
        int start = 0, end = value.length();
        if (value.startsWith("`")) {
            start++;
        }
        if (value.endsWith("`")) {
            end--;
        }
        return value.substring(start, end);
    }
}
