package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.IndexDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;


public class IndexDescriptionBuilder implements JdbcResultBuilder {
    // user_indexes
    private static final int C_INDEX_NAME = 1;
    private static final int C_TABLE = 2;
    private static final int C_TABLESPACE_NAME = 3;
    private static final int C_INDEX_TYPE = 4;
    private static final int C_UNIQUENESS = 5;
    // user_ind_columns
    private static final int C_COLUMN_NAME = 6;
    private static final int C_DESCEND = 7;

    // zur Unterscheidung von PK und anderem Index
    private static final int C_CONSTRAINT_TYPE = 8;

    private IndexDescription index;
    private CatalogDescription catalog;
    private String constraintType;

    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        index = null;
        catalog = (CatalogDescription) queryObject;
    }

    public void fetch(ResultSet row) throws SQLException {
        String idxName = row.getString(C_INDEX_NAME);
        if (index == null || !(index.getIndexName().equals(idxName))) {
            if (index != null) addIndex();
            index = new IndexDescription();
            index.setIndexName(idxName);
            index.setTableName(row.getString(C_TABLE));
            index.setTableSpace(row.getString(C_TABLESPACE_NAME));
            constraintType = row.getString(C_CONSTRAINT_TYPE);
            if ("UNIQUE".equals(row.getString(C_UNIQUENESS))) {
                index.setUnique(true);
            }
            String type = row.getString(C_INDEX_TYPE);
            /**
             DOMAIN    -- context Index CUSTOMER_SEARCHNAME_IDX
             FUNCTION-BASED BITMAP
             FUNCTION-BASED NORMAL
             IOT - TOP   -- ignorieren werden automatisch �ber context-index erzeugt
             LOB         -- ignorieren
             NORMAL
             **/
            if (type == null) type = "";
            if (type.indexOf("DOMAIN") > -1) {
                index.setContext(true);
            }
            if (type.indexOf("FUNCTION-BASED") > -1) {
                index.setFunctionBased(true);
            }
            if (type.indexOf("BITMAP") > -1) {
                index.setBitmap(true);
            }
            /*
            index.setNoSort();  // nyi
            index.setReverse(); // nyi
            */
        }
        String order = "ASC";
        if ("DESC".equals(row.getString(C_DESCEND))) {
            order = "DESC";
        }
        index.addColumn(row.getString(C_COLUMN_NAME), order);
    }

    public void close(boolean isComplete) throws SQLException {
        if (index != null) addIndex();
    }

    private void addIndex() {
        TableDescription table = catalog.getTable(index.getTableName());
        if (table != null) {
            if (constraintType.equals("P")) // PK
            {
                if (table.getPrimaryKey() != null)
                    throw new IllegalArgumentException("only one primary key expected");
                table.setPrimaryKey(index);
            } else { // CONSTRAINT
                if (table.getPrimaryKey() == null || !index.getIndexName()
                        .equals(table.getPrimaryKey().getIndexName())) {
                    table.addIndex(index);
                }
            }
        }
    }
}
