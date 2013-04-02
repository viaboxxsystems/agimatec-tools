package com.agimatec.utility.fileimport.spreadsheet;

/**
 * Description: Abstraction for a single spreadsheet row<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:50:16 <br/>
 * Copyright: Agimatec GmbH
 */
public interface IRow {
    /**
     * @return the row number in the spreadsheet
     */
    int getRowNum();
    
    int getFirstCellNum();

    int getLastCellNum();

    ICell getCell(int num);
}
