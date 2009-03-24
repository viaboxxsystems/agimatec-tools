package com.agimatec.dbmigrate.util;

import com.agimatec.commons.beans.MapQuery;
import com.agimatec.sql.script.ScriptVisitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;

/**
 * <b>Description:</b>   I_ScriptVisitor wrapper that supports conditional execution of a SQL script<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public final class ConditionalScriptVisitor extends ScriptVisitorDelegate {
    private static final Log log = LogFactory.getLog(ConditionalScriptVisitor.class);

    private Map environment = Collections.EMPTY_MAP;
    private Stack conditionStack = new Stack();
    private Stack expressionStack = new Stack();

    public ConditionalScriptVisitor(ScriptVisitor aVisitor, Map env) {
        super(aVisitor);
        environment = env;
    }

    public ConditionalScriptVisitor(ScriptVisitor aVisitor) {
        super(aVisitor);
    }

    public Map getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map aEnvironment) {
        environment = aEnvironment;
    }

    public void visitComment(String theComment) throws SQLException {
        super.visitComment(theComment);
        int idx = theComment.indexOf("#if ");
        if (idx >= 0) {
            String condition = theComment.substring(idx + 4);
            MapQuery q = new MapQuery(condition);
            expressionStack.push(condition);
            conditionStack.push(Boolean.valueOf(q.doesMatch(getEnvironment())));
            log.info(
                  "FOUND Condition: (" + q.toString() + ") = " + conditionStack.peek());
        } else if (theComment.indexOf("#endif") >= 0) {
            if (expressionStack.isEmpty()) {
                log.error(theComment + " ---> #endif without #if!");
            } else {
                log.info("END of Condition: (" + expressionStack.peek() + ")");
                conditionStack.pop();
                expressionStack.pop();
            }
        }
    }

    public boolean isConditionTrue() {
        if (conditionStack.isEmpty()) return true;
        for (Object aConditionStack : conditionStack) {
            Boolean bool = (Boolean) aConditionStack;
            if (!bool.booleanValue()) return false;
        }
        return true;
    }

    public void doCommit() throws SQLException {
        if (isConditionTrue()) {
            super.doCommit();
        } else {
            if (log.isInfoEnabled()) {
                log.info("commit - ignored because: " + conditionCause());
            }
        }
    }

    private String conditionCause() {
        return expressionStack.peek() + " --> " + conditionStack.peek();
    }

    public void doRollback() throws SQLException {
        if (isConditionTrue()) {
            super.doRollback();
        } else {
            if (log.isInfoEnabled()) {
                log.info("rollback - ignored because:" + conditionCause());
            }
        }
    }

    public int visitStatement(String statement) throws SQLException {
        if (isConditionTrue()) {
            return super.visitStatement(statement);
        } else {
            if (log.isInfoEnabled()) {
                log.info("statement: '" + statement + "' - ignored because: " +
                      conditionCause());
            }
            return 0;
        }
    }
}


