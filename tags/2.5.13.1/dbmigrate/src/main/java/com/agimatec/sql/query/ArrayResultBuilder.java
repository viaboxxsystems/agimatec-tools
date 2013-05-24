package com.agimatec.sql.query;

import java.sql.*;
import java.util.List;


/**
 * Default QueryResultVisitor that creates Object[] with the row data.
 * This class is neither thread-safe, nor stateless.
 */
public class ArrayResultBuilder implements JdbcResultBuilder {
    protected int numCols;
    protected List result;
    protected int[] colTypes;
    protected Object[] row;

    public void afterExecute(final ResultSetMetaData rsmData, final Object queryObject,
                     final List resultList) throws SQLException {
        this.numCols = rsmData.getColumnCount();
        initColTypes(rsmData);
        this.result = resultList;
        row = null;
    }

    private void initColTypes(final ResultSetMetaData rsmData) throws SQLException {
        colTypes = new int[numCols];
        for (int i = 0; i < colTypes.length; i++) {
            colTypes[i] = rsmData.getColumnType(i + 1);
            if (colTypes[i] == Types.NUMERIC || colTypes[i] == Types.DECIMAL) {
                final int scale = rsmData.getScale(i + 1);
                if (scale == 0) colTypes[i] = Types.INTEGER;
            }
        }
    }

    /**
     * create an Object[] with the data from the resultSet.
     * add one Object[] to the result List.
     */
    public void fetch(final ResultSet resultSet) throws SQLException {
        if (row != null) result.add(row);
        row = new Object[numCols];
        for (int i = 1; i <= numCols; i++) {
            switch (colTypes[i - 1]) {
                case Types.BLOB:
                    row[i - 1] = resultSet.getBytes(i);
                    break;
                case Types.INTEGER:
                case Types.SMALLINT:
                    try {
                        row[i - 1] = resultSet.getInt(i);
                    } catch (SQLException ex) {
                        // when value is too big for int (Numerischer �berlauf), change type
                        try {
                            row[i - 1] =
                                    resultSet.getBigDecimal(i); // retry with BigDecimal
                            colTypes[i - 1] =
                                    Types.BIGINT; // from now on fetch BigDecimal here.
                        } catch (SQLException ex2) {
                            ex.setNextException(ex2);
                            throw ex; // throw the first exception instead
                        }
                    }
                    break;
                case Types.NUMERIC:
                    row[i - 1] = resultSet.getDouble(i);
                    break;
                case Types.TIMESTAMP:
                    row[i - 1] = resultSet.getTimestamp(i);
                    break;
                case Types.CLOB:
                    Clob clob = resultSet.getClob(i);
                    if (clob != null) {
                        row[i - 1] = clob.getSubString(1, (int) clob.length());
                    } else {
                        row[i - 1] = null;
                    }
                    break;
                case Types.BINARY:
                case Types.VARBINARY:
                    row[i - 1] = resultSet.getBytes(i);
                    break;
                case Types.BIGINT:
                    row[i - 1] = resultSet.getBigDecimal(i);
                    break;
                default:
                    row[i - 1] = resultSet.getObject(i);
            }
        }
    }

    public void close(boolean isComplete) {
        if (row != null) result.add(row);
    }
}
