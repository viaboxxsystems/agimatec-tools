package com.agimatec.sql.query;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>Description: QueryResultVisitor that can be used
 * for queries that return an answer set with one String column, e.g.
 * useful for SELECT Name ... statements with one or multiple result rows.</p>
 *
 * @author Roman Stumm
 */
public class StringResultBuilder implements JdbcResultBuilder {
    protected List<String> result;

    public void afterExecute(final ResultSetMetaData data, final Object queryObject,
                             final List resultList) {
        result = resultList;
    }

    /**
     * fetch the next row that contains one String column.
     *
     * @throws SQLException
     */
    public void fetch(final ResultSet row) throws SQLException {
        result.add(row.getString(1));
    }

    public void close(boolean isComplete) {
    }
}

