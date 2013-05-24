package com.agimatec.sql.script;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.jdbc.JdbcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLScriptExecutor implements ScriptVisitor {
    private static final Logger myLogger = LoggerFactory.getLogger(SQLScriptExecutor.class);
    protected final static Logger mySQLLogger = LoggerFactory.getLogger("commons.sql");
    private JdbcDatabase store;

    public SQLScriptExecutor(JdbcDatabase aStore) {
        store = aStore;
    }

    protected Connection getConnection() {
        return store.getConnection();
    }

    public int visitStatement(String statement) throws SQLException {
        mySQLLogger.info(statement);
        if (getConnection() == null) {
            throw new JdbcException("cannot exec: " + statement + ", because 'not connected to database'");
        }
        Statement stmt = getConnection().createStatement();
        try {
            return stmt.executeUpdate(statement);
        } finally {
            stmt.close();
        }
    }

    public void visitComment(String theComment) {
        log("Comment: " + theComment);
    }

    public void doCommit() throws SQLException {
        mySQLLogger.info("commit");
        if (getConnection() == null) {
            throw new JdbcException("cannot commit, because 'not connected to database'");
        }
        store.commit();
    }

    public void doRollback() throws SQLException {
        mySQLLogger.info("rollback");
        if (getConnection() == null) {
            throw new JdbcException("cannot rollback, because 'not connected to database'");
        }
        store.rollback();
    }

    public void log(Object obj) {
        if (obj instanceof Throwable) {
            myLogger.error(null, (Throwable) obj);
        } else {
            myLogger.info(String.valueOf(obj));
        }
    }

}
