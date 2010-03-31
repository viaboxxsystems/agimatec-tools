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
 * Copyright: Agimatec GmbH
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

    /** used to create field name */
    public String toString() {
        return getStringValue();
    }

    public Object getValue() {
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
            case HSSFCell.CELL_TYPE_FORMULA:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
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
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
            case HSSFCell.CELL_TYPE_FORMULA:
                return cell.getNumericCellValue();
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? 1.0 : 0.0;
            case HSSFCell.CELL_TYPE_STRING:
                return Double.parseDouble(cell.getRichStringCellValue().getString());
            case HSSFCell.CELL_TYPE_ERROR:
                return (double) cell.getErrorCellValue();
            default:
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
            return cell.getDateCellValue();
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
