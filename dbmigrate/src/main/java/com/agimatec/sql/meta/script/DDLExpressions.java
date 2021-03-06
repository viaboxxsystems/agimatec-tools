package com.agimatec.sql.meta.script;

import com.agimatec.commons.config.Config;
import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.config.TextNode;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.mysql.MySqlDDLExpressions;
import com.agimatec.sql.meta.oracle.OracleDDLExpressions;
import com.agimatec.sql.meta.postgres.PostgresDDLExpressions;

import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.05.2007 <br/>
 * Time: 10:36:52 <br/>
 */
public abstract class DDLExpressions {
    public abstract ExtractExpr[] getExpressions();

    public static DDLExpressions forDbms(String dbms) {
        if ("oracle".equalsIgnoreCase(dbms)) {
            return new OracleDDLExpressions();
        } else if ("postgres".equalsIgnoreCase(dbms)) {
            return new PostgresDDLExpressions();
        } else if ("mysql".equalsIgnoreCase(dbms)) {
            return new MySqlDDLExpressions();
        } else return null;
    }

    protected DDLExpressions() {
    }

    public ExtractExpr getExpression(String expName) {
        for (ExtractExpr each : getExpressions()) {
            if (expName.equals(each.getName())) return each;
        }
        return null;
    }

    /**
     * equalize type names (between schema-import and ddl-parsing)
     *
     * @param cd
     */
    public abstract void equalizeColumn(ColumnDescription cd);

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * remove \"
     *
     * @param value - value or null to strip
     * @return stripped value or null
     */
    public String strip(String value) {
        if (value == null) return null;
        int start = 0, end = value.length();
        if (value.startsWith("\"")) {
            start++;
        }
        if (value.endsWith("\"")) {
            end--;
        }
        return value.substring(start, end);
    }

    public static ExtractExpr[] compileExpressions(String ddlXmlPath) {
        Config cfg = ConfigManager.createForClasspath().readConfig(ddlXmlPath);
        return DDLScriptSqlMetaFactory.compileExpressions(convertToArray(cfg.getList("ddl")));
    }

    private static String[] convertToArray(List<TextNode> texts) {
        String[] stmtFormats = new String[texts.size()];
        int i = 0;
        for (TextNode each : texts) {
            stmtFormats[i++] = each.getValue().trim().replace("\n", "");
        }
        return stmtFormats;
    }
}
