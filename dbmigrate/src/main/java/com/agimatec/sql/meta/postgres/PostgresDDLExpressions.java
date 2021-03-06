package com.agimatec.sql.meta.postgres;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.ExtractExpr;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 24.04.2007 <br/>
 * Time: 19:00:56 <br/>
 */
public class PostgresDDLExpressions extends DDLExpressions {
    public static final ExtractExpr[] expressions;


    static {
        expressions = compileExpressions("com/agimatec/sql/meta/postgres-ddl.xml");
    }

    public ExtractExpr[] getExpressions() {
        return expressions;
    }

    @Override
    public void equalizeColumn(ColumnDescription cd) {
        // ignore
    }

}