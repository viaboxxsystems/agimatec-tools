package com.agimatec.dbtransform;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.ExtractExpr;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 17.12.2007 <br/>
 * Time: 16:42:43 <br/>
 * Copyright: Agimatec GmbH
 */
public class SomeDDLExpressions extends DDLExpressions {
    private final DDLExpressions all;
    private ExtractExpr[] someExpressions = new ExtractExpr[0];

    public SomeDDLExpressions(DDLExpressions all) {
        this.all = all;
    }

    public void addExpression(String expName) {
        ExtractExpr exp = all.getExpression(expName);
        ExtractExpr[] exps = new ExtractExpr[someExpressions.length + 1];
        if (someExpressions.length > 0) {
            System.arraycopy(someExpressions, 0, exps, 0, someExpressions.length);
        }
        someExpressions = exps;
        someExpressions[someExpressions.length-1] = exp;
    }

    public ExtractExpr[] getExpressions() {
        return someExpressions;
    }

    public void equalizeColumn(ColumnDescription cd) {
        all.equalizeColumn(cd);
    }
}
