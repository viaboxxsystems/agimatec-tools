package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.query.JdbcResultBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;


public class ColumnDescriptionBuilder implements JdbcResultBuilder {
    private static final int C_COLUMN_NAME = 1;
    private static final int C_DATA_TYPE = 2;
    private static final int C_CHAR_LENGTH = 3;
    private static final int C_DATA_PRECISION = 4;
    private static final int C_DATA_SCALE = 5;
    private static final int C_NULLABLE = 6;
    private static final int C_TABLE_NAME = 7;
    private static final int C_DATA_TYPE_OWNER = 8;

    private CatalogDescription catalog;
    /**
     * data_type:
     BLOB
     CHAR
     CLOB
     DATE
     LONG
     LONG RAW
     NUMBER
     RAW
     ROWID
     TIMESTAMP(0)
     VARCHAR2
     **/
    /**
     * nullable:
     * N, Y
     */
    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        catalog = (CatalogDescription) queryObject;
    }

    public void fetch(ResultSet row) throws SQLException {
        final ColumnDescription column = new ColumnDescription();
        String tableName = row.getString(C_TABLE_NAME);
        final TableDescription table = catalog.getTable(tableName);
        if (table != null) {
            column.setColumnName(row.getString(C_COLUMN_NAME));
            column.setNullable(!"N".equals(row.getString(C_NULLABLE)));
            column.setTypeName(row.getString(C_DATA_TYPE));
            String owner = row.getString(C_DATA_TYPE_OWNER);
            if (StringUtils.isNotEmpty(owner)) {
                column.setTypeName(owner.trim() + "." + column.getTypeName());
            }
            if ("TIMESTAMP(0)".equals(column.getTypeName())) {
                column.setTypeName("TIMESTAMP");
            }
            if (column.getTypeName().equals("VARCHAR2")) {
                column.setTypeName("VARCHAR");
            }
            if (isAnyOf(column.getTypeName(),
                    new String[]{"VARCHAR", "CHAR", "RAW", "NVARCHAR2"})) {
                column.setPrecisionEnabled(true);
                column.setPrecision(row.getInt(C_CHAR_LENGTH));
            } else if (column.getTypeName().equals("NUMBER")) {
                column.setPrecisionEnabled(true);
                column.setPrecision(row.getInt(C_DATA_PRECISION));
                column.setScale(row.getInt(C_DATA_SCALE));
            }
            table.addColumn(column);
        }
    }

    private boolean isAnyOf(String value, String[] values) {
        return ArrayUtils.indexOf(values, value, 0) > -1;
    }

    public void close(boolean isComplete)  {
    }

}

