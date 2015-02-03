package com.agimatec.utility.fileimport.spreadsheet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

/**
 * Description: Abstraction for a single excel-spreadsheet cell <br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:21:33 <br/>
 */
public class ExcelCell implements ICell {
  private final DecimalFormat plainNumericFormat = new DecimalFormat("#.#");
  private final Cell cell;
  private CellStyle style;

  public ExcelCell(Cell hssfCell) {
    cell = hssfCell;
    plainNumericFormat.setGroupingUsed(false);
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator('.');
    plainNumericFormat.setDecimalFormatSymbols(dfs);
  }

  /**
   * used to create field name
   */
  public String toString() {
    return getStringValue();
  }

  public Object getValue() {
    return getValue(cell.getCellType());
  }

  private Object getValue(int cellType) {
    switch (cellType) {
      case HSSFCell.CELL_TYPE_NUMERIC:
        if (HSSFDateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue();
        } else {
          return cell.getNumericCellValue();
        }
      case HSSFCell.CELL_TYPE_FORMULA:
        return getValue(cell.getCachedFormulaResultType());
      case HSSFCell.CELL_TYPE_BOOLEAN:
        return cell.getBooleanCellValue();
      case HSSFCell.CELL_TYPE_STRING:
        return cell.getRichStringCellValue().getString();
      case HSSFCell.CELL_TYPE_ERROR:
        return cell.getErrorCellValue();
      default:
        return null;
      // do not handle Formular, Error, Blank, ...
    }
  }

  public double getNumericValue() {
    return toNumericValue(getValue());
  }

  private double toNumericValue(Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else if (value instanceof Boolean) {
      return ((Boolean) value) ? 1.0 : 0.0;
    } else if (value instanceof String) {
      return Double.parseDouble((String) value);
    } else {
      return 0.0;
    }
  }

  public String getStringValue() {
    Object val = getValue();
    if (val == null) return null;
    else if (val instanceof String) return (String) val;
    else if (val instanceof Double) {
      synchronized (plainNumericFormat) {
        return plainNumericFormat.format(val);
      }
    } else return String.valueOf(val);
  }

  public Date getDateValue() {
    if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC ||
        cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
        try {
            return cell.getDateCellValue();
        } catch(NullPointerException ex) { // workaround for bug in POI 3.11
            // NullPointerException at org.apache.poi.ss.usermodel.DateUtil.getJavaDate(DateUtil.java:231) ~[poi-3.11.jar:3.11]
            // TODO RSt - remove when https://issues.apache.org/bugzilla/show_bug.cgi?id=57512 is fixed
            return null;
        }
    } else {
      return null;
    }
  }
/*
    public short getFillForegroundColor() {
        return getStyle().getFillForegroundColor();
    }
*/

  public CellStyle getStyle() {
    if (style == null) {
      style = cell.getCellStyle();
    }
    return style;
  }

  public Cell getCell() {
    return cell;
  }

  public String getComment() {
    Comment comment = cell.getCellComment();
    if (comment != null) {
      return comment.getString().getString();
    } else {
      return null;
    }
  }

  public int getColumnIndex() {
    return cell.getColumnIndex();
  }
}
