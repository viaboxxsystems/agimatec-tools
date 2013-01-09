package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.ForeignKeyDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;


public class ConstraintCommentBuilder implements JdbcResultBuilder {
    private static final int TABLE_NAME = 1;
    private static final int CONSTRAINT_NAME = 2;
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
            ForeignKeyDescription constraint =
                    table.getForeignKey(row.getString(CONSTRAINT_NAME));
            if (constraint != null) {
                constraint.setComment(row.getString(COMMENTS));
            }
        }
    }
}
