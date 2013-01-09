package com.agimatec.sql.script;

import java.sql.SQLException;

/**
 * Interface -
 * a class that visits parsed SQL statements.
 * <p/>
 *
 */
public interface ScriptVisitor {

    /**
     * @param statement
     */
    int visitStatement(String statement) throws SQLException;

    /**
     * @param theComment
     */
    void visitComment(String theComment) throws SQLException;

    /**
     *
     */
    void doCommit() throws SQLException;

    /**
     *
     */
    void doRollback() throws SQLException;
}
