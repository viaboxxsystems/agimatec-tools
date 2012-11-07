package com.agimatec.sql.query;

import com.agimatec.sql.SQLStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper class, that implements some general methods for query processing.
 * <p/>
 * This class offers serveral interfaces with APIs for easy
 * query execution, SQL-generation/execution etc.
 * See interfaces for details.
 * <p/>
 * (This class could be used anywhere outside an EJB environment.)
 */
public class JdbcQueryUtil extends JdbcExecutor implements Serializable {
    protected final Connection connection;
    protected final SQLBuilder sqlBuilder;
    protected static final Log log = LogFactory.getLog(JdbcQueryUtil.class);

    /** Use StoreQueryBeanBuilder.create() instead of this constructor! */
    public JdbcQueryUtil(final Connection aConnection, final SQLBuilder aSqlBuilder) {
        this.connection = aConnection;
        this.sqlBuilder = aSqlBuilder;
    }

    public Connection getConnection() {
        return connection;
    }

    public SQLBuilder getSqlBuilder() {
        return sqlBuilder;
    }

    /**
     * execute a insert, update or delete statement.
     *
     * @return either the row count for INSERT, UPDATE or DELETE statements,
     *         or 0 for SQL statements that return nothing
     * @throws SQLException
     */
    public int executeUpdate(final QueryDefinition updateDefinition)
            throws SQLException {
        final SQLStatement sql = sqlBuilder.generateSQL(updateDefinition);
        return execRowsAffected(sql);
    }

    /**
     * Convenience method -
     * Execute a Query that returns ONE Row with one int value.
     * Useful for executing SELECT COUNT(*) statements.
     *
     * @return the int value of the first column, first row in the answer set.
     *         if the answer set is empty, return -1.
     */
    public int executeCount(final QueryDefinition queryDefinition) throws Exception {
        final QueryResult result = executeQuery(queryDefinition);
        if (result.getList().isEmpty()) return -1;
        final Object row = result.getList().get(0);
        if (row instanceof Number) { // assume a Number
            return ((Number) row).intValue();
        } else { // assume Object[] with a Number
            Object[] theArray = (Object[]) row;
            if (theArray == null || theArray.length == 0) return -1;
            if (theArray[0] instanceof Number) {
                return ((Number) (theArray)[0]).intValue();
            } else {
                return -1;
            }
        }
    }

    /**
     * execute a select statement returning objects build by
     * the result visitor.
     * The default result objects are of type Object[], one for each row in the resultset.
     *
     * @param queryDefinition object containing the query specification
     * @return the lookupresult containing the results loaded
     * @throws Exception
     */
    public QueryResult executeQuery(final QueryDefinition queryDefinition) throws
            SQLException {
        final SQLStatement query = sqlBuilder.generateSQL(queryDefinition);
        final JdbcResultBuilder resultBuilder = newResultVisitor(
                sqlBuilder.getResultBuilderName(queryDefinition.getQueryName()));
        return fetchResult(query, resultBuilder, queryDefinition);
    }

}
