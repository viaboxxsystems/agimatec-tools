package com.agimatec.sql.meta.script;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.oracle.OracleDDLExpressions;
import com.agimatec.sql.meta.postgres.PostgresDDLExpressions;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.05.2007 <br/>
 * Time: 10:36:52 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class DDLExpressions {
    public abstract ExtractExpr[] getExpressions();

    public static DDLExpressions forDbms(String dbms) {
        if ("oracle".equalsIgnoreCase(dbms)) {
            return new OracleDDLExpressions();
        } else if ("postgres".equalsIgnoreCase(dbms)) {
            return new PostgresDDLExpressions();
        } else return null;
    }

    protected DDLExpressions() {
    }

    public ExtractExpr getExpression(String expName)
    {
        for(ExtractExpr each : getExpressions()) {
            if(expName.equals(each.getName())) return each;
        }
        return null;
    }

    /**
     * equalize type names (between schema-import and ddl-parsing)
     * @param cd
     */
    public abstract void equalizeColumn(ColumnDescription cd);

    @Override
    public String toString() {
        return getClass().getName();
    }
}
