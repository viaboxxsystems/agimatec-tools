
package com.agimatec.sql;

import org.apache.commons.logging.Log;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a SQLStatement with its parameters.
 */
public class SQLStatement {
    private final List parameters;
    private final String sqlStatement;

    /**
     * Construct with the given sqlStatementString.
     * You may provide the parameters with getParameters().add() afterwards.
     */
    public SQLStatement(final String stmtString) {
        this(stmtString, new ArrayList());
    }

    /**
     * Construct with a given sqlStatementString an a parameter list
     */
    public SQLStatement(final String stmtString, final List params) {
        parameters = params;
        sqlStatement = stmtString;
    }

    /**
     * Return the sql statement string
     */
    public String getStatement() {
        return sqlStatement;
    }

    /**
     * Return the statement parameters
     */
    public List getParameters() {
        return parameters;
    }

    /**
     * Add the given value to the receiver's parameter list.
     */
    public void addParameter(final Object value) {
        parameters.add(value);
    }

    /**
     * Create a new JDBC PreparedStatement
     * with the receiver's parameters.
     */
    public PreparedStatement asPreparedStatement(final Connection conn)
            throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement(sqlStatement);
        provideParameters(stmt);
        return stmt;
    }

    /**
     * Set the receiver's parameters into the given PreparedStatement.
     * CAUTION: the given statement must match with the receiver!
     */
    public void provideParameters(final PreparedStatement stmt) throws SQLException {
        if (parameters.isEmpty()) return;

        /*   final OraclePreparedStatement oraStmt =
                        (stmt instanceof OraclePreparedStatement) ?
                                (OraclePreparedStatement) stmt : null;
        */
        for (int i = 0; i < parameters.size();) {
            Object pval = parameters.get(i++);
            if (pval == null) {
                stmt.setNull(i, Types.VARCHAR);
                /*  } else if (oraStmt != null && pval instanceof String) {
                     oraStmt.setFixedCHAR(i, (String) pval);
                */
            } else {
                stmt.setObject(i, pval);
            }
        }
    }

    /**
     * Logging convenience -
     * Log the receiver's sql (with ? instead of parameters) and parameters (separetely) onto the logdevice under info priority
     */
    public void logInfo(final Log log) {
        if (log.isInfoEnabled()) {
            log.info(getStatement());
            final StringBuilder buf = new StringBuilder();
            buf.append("Params:");
            for (int i = 0; i < getParameters().size(); i++) {
                String val = String.valueOf(getParameters().get(i));
                if(val.length()>20) val=val.substring(0, 15) + "...";
                buf.append(" <").append(val).append("> --");
            }
            log.info(buf.toString());
        }
    }

    /**
     * return the statement including parameter values instead of ?
     */
    public String getParameterizedStatement() {
        try {
            final StringWriter writer = new StringWriter();
            final SQLStringGenerator sqlGen =
                    new SQLStringGenerator(getStatement(), writer, getParameters());
            sqlGen.parse();
            return writer.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    public String toString() {
        return super.toString() + "[" + getParameterizedStatement() + "]";
    }

    /**
     * return a copy of the receiver or the receiver itself (when not changed)
     * that is ready to be executed with asPreparedStatement(connection) where
     * the SQL and parameters have been transformed so that execution result is
     * the same as when the receiver would have been transformed by getParameterizedStatement().
     * <p/>
     * into adequate strings and modifies the statement so that correct behavior is guaranteed.
     * Note: Parameters of Collection types are generated into the statement as separate parameters.
     */
    public SQLStatement getPreparedStatement() {
        if (parameters.isEmpty()) return this;

        try {
            final StringWriter writer = new StringWriter();
            final SQLStmtPreparer sqlGen =
                    new SQLStmtPreparer(sqlStatement, writer, parameters);
            sqlGen.parse();
            return new SQLStatement(writer.toString(), sqlGen.getNewParams());
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }
}


