package com.agimatec.utility.fileimport.spreadsheet;

import org.apache.poi.hssf.usermodel.HSSFRow;

/**
 * Description: Abstraction for a single excel-spreadsheet row<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:50:21 <br/>
 * Copyright: Agimatec GmbH
 */
public class ExcelRow implements IRow {
    final HSSFRow row;

    public ExcelRow(HSSFRow hssfRow) {
        row = hssfRow;
    }

    public HSSFRow getRow() {
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
}
