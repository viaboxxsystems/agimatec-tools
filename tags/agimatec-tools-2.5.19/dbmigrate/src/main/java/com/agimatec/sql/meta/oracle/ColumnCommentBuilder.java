package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ColumnCommentBuilder implements JdbcResultBuilder {
    private static final int TABLE_NAME = 1;
    private static final int COLUMN_NAME = 2;
    private static final int COMMENTS = 3;

    private CatalogDescription catalog;

    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        catalog = (CatalogDescription) queryObject;
    }

    public void close(boolean isComplete)  {
    }

    public void fetch(ResultSet row) throws SQLException {
        final TableDescription table = catalog.getTable(row.getString(TABLE_NAME));
        if (table != null) {
            ColumnDescription column = table.getColumn(row.getString(COLUMN_NAME));
            if (column != null) {
                column.setComment(row.getString(COMMENTS));
            }
        }
    }
}
