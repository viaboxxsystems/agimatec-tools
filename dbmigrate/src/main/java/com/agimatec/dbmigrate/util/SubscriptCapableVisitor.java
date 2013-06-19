package com.agimatec.dbmigrate.util;

import com.agimatec.sql.script.SQLScriptParser;
import com.agimatec.sql.script.ScriptVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * <b>Description:</b>   visitor able to invoke subscripts
 * with oracle @ syntax<br>
 *
 * @author Roman Stumm
 * @ ->  iterateSQLScript  (semicolon separated)
 * @; ->  iterateSQLScript (semicolon separated)
 * @> -> execSQLScript     (script as a single statement)
 * @| -> iterateSQLLines   (execute linewise)
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 */
public class SubscriptCapableVisitor extends ScriptVisitorDelegate {
    private static final Logger log = LoggerFactory.getLogger(SubscriptCapableVisitor.class);
    private final SQLScriptParser parser;

    public SubscriptCapableVisitor(ScriptVisitor nextVisitor, SQLScriptParser parser) {
        super(nextVisitor);
        this.parser = parser;
    }

    public int visitStatement(String statement) throws SQLException {
        if (statement.startsWith("@")) {
            try {
                if (statement.charAt(1) == '>') {
                    parser.execSQLScript(this, statement.substring(2));
                } else if (statement.charAt(1) == '|') {
                    parser.iterateSQLLines(this, statement.substring(2));
                } else if (statement.charAt(1) == ';') {
                    parser.iterateSQLScript(this, statement.substring(2));
                } else {
                    parser.iterateSQLScript(this, statement.substring(1));
                }
            } catch (Exception e) {
                log.error("error executing subscript: " + statement.substring(1), e);
                throw new SQLException(e.getMessage(), e);
            }
            return 0;
        } else if (statement.length() > 5 && statement.substring(0, 4).toLowerCase().equals("set ")) {
            doSetExpression(statement.substring(4));
            return 0;
        } else {
            return super.visitStatement(statement);
        }
    }

    private void doSetExpression(String expression) {
        StringTokenizer tokens = new StringTokenizer(expression, "=,; ", true);

        String varName = nextToken(tokens, expression);
        while (tokens.hasMoreTokens()) {
            String nt = nextToken(tokens, expression);
            if (!"=".equals(nt) && !" ".equals(nt)) {
                log.warn("Illegal operator, expected '=' in: " + expression);
                return;
            } else if ("=".equals(nt)) break;
        }
        String value = nextToken(tokens, expression);
        if (varName == null || value == null) return;
        if (varName.equals("FAIL_ON_ERROR")) { // derzeit nur 1 Variable, derzeit noch hard-coded. 
            boolean bool = Boolean.parseBoolean(value);
            log.info("SET " + varName + "=" + bool + ";");
            parser.setFailOnError(bool);
        } else {
            log.warn("Illegal script set-option: " + expression);
        }
    }

    private String nextToken(StringTokenizer tokens, String expression) {
        if (!tokens.hasMoreTokens()) {
            log.warn("Illegal script set-option: " + expression);
            return null;
        }
        return tokens.nextToken();
    }


}



