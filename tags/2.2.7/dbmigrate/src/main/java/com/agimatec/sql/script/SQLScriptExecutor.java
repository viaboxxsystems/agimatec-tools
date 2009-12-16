package com.agimatec.sql.script;

import com.agimatec.jdbc.JdbcDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLScriptExecutor implements ScriptVisitor {
    private static final Log myLogger = LogFactory.getLog(SQLScriptExecutor.class);
    protected final static Log mySQLLogger = LogFactory.getLog("commons.sql");
    private JdbcDatabase store;

    public SQLScriptExecutor(JdbcDatabase aStore) {
        store = aStore;
    }

    protected Connection getConnection() {
        return store.getConnection();
    }

    public int visitStatement(String statement) throws SQLException {
        mySQLLogger.info(statement);
        Statement stmt = getConnection().createStatement();
        try {
            return stmt.executeUpdate(statement);
        } catch(SQLException ex) {
            getConnection().rollback();
            throw ex;
        } finally {
            stmt.close();
        }
    }

    public void visitComment(String theComment) {
        log("Comment: " + theComment);
    }

    public void doCommit() throws SQLException {
        mySQLLogger.info("commit");
        getConnection().commit();
    }

    public void doRollback() throws SQLException {
        mySQLLogger.info("rollback");
        getConnection().rollback();
    }

    public void log(Object obj) {
        if (obj instanceof Throwable) {
            myLogger.error(null, (Throwable) obj);
        } else {
            myLogger.info(obj);
        }
    }

}
