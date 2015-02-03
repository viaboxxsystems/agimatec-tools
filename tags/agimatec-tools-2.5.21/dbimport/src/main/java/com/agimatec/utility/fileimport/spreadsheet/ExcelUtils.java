package com.agimatec.utility.fileimport.spreadsheet;

import java.util.Map;

/**
 * Description: Provides commonly used methods to read data from excel cells<br>
 * <p>
 * User: roman.stumm<br>
 * Date: 23.05.13<br>
 * Time: 15:11<br>
 * viaboxx GmbH, 2013
 * </p>
 *
 * @since 2.5.13
 */
public class ExcelUtils {
    public static String getString(Map<String, ICell> row, String colName) {
        ICell cell = row.get(colName);
        if (cell != null) {
            return cell.getStringValue();
        } else {
            return null;
        }
    }

    public static boolean getBoolean(Map<String, ICell> row, String columnName) {
        ICell cell = row.get(columnName);
        if (cell == null) return false;
        Object value = cell.getValue();
        if (value instanceof Boolean) return (Boolean) cell.getValue();
        else if (value instanceof Number) return ((Number) value).intValue() != 0;
        else if (value != null) {
            String content = cell.getStringValue().trim();
            return ("1".equalsIgnoreCase(content) || "yes".equalsIgnoreCase(content) ||
                    "true".equalsIgnoreCase(content));
        } else {
            return false;
        }
    }

    public static Integer getInteger(Map<String, ICell> row, String columnName) {
        ICell cell = row.get(columnName);
        if (cell == null) {
            return null;
        } else {
            Object val = cell.getValue();
            if (val instanceof Number) return ((Number) val).intValue();
            else return null;
        }
    }

    public static Long getLong(Map<String, ICell> row, String columnName) {
        ICell cell = row.get(columnName);
        if (cell == null) {
            return null;
        } else {
            Object val = cell.getValue();
            if (val instanceof Number) return ((Number) val).longValue();
            else return null;
        }
    }

    public static Double getDouble(Map<String, ICell> row, String columnName) {
        ICell cell = row.get(columnName);
        if (cell == null) {
            return null;
        } else {
            Object val = cell.getValue();
            if (val == null) return null;
            else return cell.getNumericValue();
        }
    }
}
