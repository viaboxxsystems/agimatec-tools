package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.ExtractExpr;

/**
 * <b>Description:</b>
 * Build a CatalogDescription by parsing a SQL DDL script.
 * Caution: this class not not thread-safe. <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public class OracleDDLExpressions extends DDLExpressions {
    public static final ExtractExpr[] expressions;

    static {
        expressions = compileExpressions("com/agimatec/sql/meta/oracle-ddl.xml");
    }

    public ExtractExpr[] getExpressions() {
        return expressions;
    }

    /**
     * equalize type names (between schema-import and ddl-parsing)
     *
     * @param cd
     */
    public void equalizeColumn(ColumnDescription cd) {
        if (cd.getTypeName().equalsIgnoreCase("INTEGER")) {
            cd.setTypeName("NUMBER");
        } else if (cd.getTypeName().equalsIgnoreCase("VARCHAR2")) {
            cd.setTypeName("VARCHAR");
        } else if (cd.getTypeName().equalsIgnoreCase("timestamp")) {
            cd.setTypeName(
                    "TIMESTAMP(6)"); // oracle delivers TIMESTAMP(6) when column is created as TIMESTAMP
        } else if (cd.getTypeName().equalsIgnoreCase("double")) {
            cd.setTypeName(
                    "FLOAT"); // oracle delivers FLOAT when column is created as DOUBLE
        } else if (cd.getTypeName().equalsIgnoreCase("CHARACTER")) {
            cd.setTypeName("CHAR");
        }

    }
}
