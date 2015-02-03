package com.agimatec.utility.fileimport.spreadsheet;

import org.apache.poi.ss.usermodel.Row;

/**
 * Description: Abstraction for a single excel-spreadsheet row<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:50:21 <br/>
 */
public class ExcelRow implements IRow {
    private final Row row;

    public ExcelRow(Row hssfRow) {
        row = hssfRow;
    }

    public Row getRow() {
        return row;
    }

    public int getRowNum() {
        return row.getRowNum();
    }

    public int getFirstCellNum() {
        return row.getFirstCellNum();
    }

    public int getLastCellNum() {
        return row.getLastCellNum();
    }

    public ICell getCell(int num) {
        return new ExcelCell(row.getCell(num));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ExcelRow");
        sb.append("{rowNum=").append(getRowNum());
        sb.append('}');
        return sb.toString();
    }
}
