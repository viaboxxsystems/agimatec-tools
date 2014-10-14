package com.agimatec.dbmigrate.util;

import com.agimatec.sql.script.ScriptVisitor;

import java.sql.SQLException;

/**
 * Description: subclass this class<br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 17:41:24 <br/>
 */
public abstract class ScriptVisitorDelegate implements ScriptVisitor {
    protected final ScriptVisitor nextVisitor;

    protected ScriptVisitorDelegate(ScriptVisitor aVisitor) {
        nextVisitor = aVisitor;
    }

    public int visitStatement(String statement) throws SQLException {
        if (nextVisitor == null) {
            return 0;
        } else {
            return nextVisitor.visitStatement(statement);
        }
    }

    public void visitComment(String theComment) throws SQLException {
        if (nextVisitor != null) nextVisitor.visitComment(theComment);
    }

    public void doCommit() throws SQLException {
        if (nextVisitor != null) nextVisitor.doCommit();
    }

    public void doRollback() throws SQLException {
        if (nextVisitor != null) nextVisitor.doRollback();
    }
}
