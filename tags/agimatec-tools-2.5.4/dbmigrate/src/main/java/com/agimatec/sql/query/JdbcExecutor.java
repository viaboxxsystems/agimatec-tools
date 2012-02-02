package com.agimatec.sql.query;

import com.agimatec.sql.SQLStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Abstract superclass of classes that execute direct sql statements.
 *
 * @author Roman Stumm
 */
public abstract class JdbcExecutor {

    protected final static Log log = LogFactory.getLog("agimatec.sql.meta");

    /**
     * execute a SQL-QUERY (select) and return a LookupResult
     *
     * @param aQuery          - the SQLStatement
     * @param resultBuilder   - a result visitor that builds the result rows or objects
     * @param queryDefinition - the max. number of rows to build (or GenericQuery.C_Unlimited)
     * @return a LookupResult
     * @throws Exception
     */
    protected QueryResult fetchResult(final SQLStatement aQuery,
                                      final JdbcResultBuilder resultBuilder,
                                      QueryDefinition queryDefinition) throws SQLException {
        final int maxResults = queryDefinition.getMaxResults();
        final ArrayList result = (maxResults == QueryDefinition.UNLIMITED) ? new ArrayList() :
                new ArrayList(maxResults);

        final Connection connection = getConnection();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            if (log.isInfoEnabled()) {
                log.info(aQuery.getParameterizedStatement());
            }
            stmt = aQuery.getPreparedStatement()
                    .asPreparedStatement(connection);
            resultSet = stmt.executeQuery();

            resultBuilder.afterExecute(resultSet.getMetaData(), queryDefinition.getQueryObject(),
                    result);
            boolean hasnext = resultSet.next();
            while ((maxResults == QueryDefinition.UNLIMITED || result.size() < maxResults) &&
                    hasnext) {
                resultBuilder.fetch(resultSet);
                hasnext = resultSet.next();
            }
            resultBuilder.close(!hasnext);
            return new QueryResult(result, !hasnext);
        } catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                log.error("Error executing SQL: " + aQuery.getParameterizedStatement(), ex);
            }
            throw ex;
        } finally {
            if (resultSet != null) resultSet.close();
            if (stmt != null) stmt.close();
        }
    }

    /**
     * execute an sql (update/delete/insert) statement and return the number of rows affected
     *
     * @return the number of rows affected (if the database provides that info)
     * @throws SQLException
     */
    protected int execRowsAffected(final SQLStatement aSQL) throws SQLException {
        final Connection connection = getConnection();
        if (log.isInfoEnabled()) {
            log.info(aSQL.getParameterizedStatement());
        }
        final PreparedStatement stmt = aSQL.getPreparedStatement().asPreparedStatement(connection);
        int result;
        try {
            result = stmt.executeUpdate();
        } catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                log.error("Error executing SQL: " + aSQL.getParameterizedStatement(), ex);
            }
            throw ex;
        } finally {
            stmt.close();
        }
        return result;
    }


    protected JdbcResultBuilder newResultVisitor(final String className) {
        if (className == null) { // return default instance
            return new ArrayResultBuilder();
        }
        try { // return specific, custom instance
            return ((Class<JdbcResultBuilder>) Class.forName(className)).newInstance();
        } catch (Exception ex) {
            log.error(null, ex);
            return null;
        }
    }

    public abstract Connection getConnection();
}
