package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class TableCommentBuilder implements JdbcResultBuilder {
    private static final int TABLE_NAME = 1;
    private static final int COMMENTS = 2;

    private CatalogDescription catalog;

    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        catalog = (CatalogDescription) queryObject;
    }

    public void close(boolean isComplete) throws SQLException {
    }

    public void fetch(ResultSet row) throws SQLException {
        final TableDescription table = catalog.getTable(row.getString(TABLE_NAME));
        if (table != null) {
            table.setComment(row.getString(COMMENTS));
        }
    }
}
