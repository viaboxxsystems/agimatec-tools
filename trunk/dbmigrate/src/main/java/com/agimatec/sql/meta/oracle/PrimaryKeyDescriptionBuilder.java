package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.IndexDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class PrimaryKeyDescriptionBuilder implements JdbcResultBuilder {
    private static final int C_CONSTRAINT_NAME = 1;
    private static final int C_TABLE_NAME = 2;

    private CatalogDescription catalog;

    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        catalog = (CatalogDescription) queryObject;
    }

    public void fetch(ResultSet row) throws SQLException {
        String table = row.getString(C_TABLE_NAME);
        TableDescription tableDesc = catalog.getTable(table);
        if (tableDesc != null) {
            String constraint = row.getString(C_CONSTRAINT_NAME);
            IndexDescription pkIndex = tableDesc.getIndex(constraint);
            if (pkIndex != null) {
                tableDesc.setPrimaryKey(pkIndex);
                tableDesc.removeIndex(constraint);
            }
        }
    }

    public void close(boolean isComplete) throws SQLException {
    }

}
