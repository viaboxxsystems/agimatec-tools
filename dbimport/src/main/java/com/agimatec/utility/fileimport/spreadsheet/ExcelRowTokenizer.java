package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.LineTokenizer;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Iterator;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:13:30 <br/>
 * Copyright: Agimatec GmbH
 */
public class ExcelRowTokenizer implements LineTokenizer<ExcelRow, ExcelCell> {
    private Iterator<Cell> cellIterator;
//    private final ExcelRow row;

    public ExcelRowTokenizer(ExcelRow aLine) {
//        row = aLine;
        cellIterator = aLine.row.cellIterator();
    }

    public boolean isLineIncomplete() {
        return false;
    }

    public ExcelCell continueParse(ExcelCell aSingleValue, ExcelRow aRecord) {
        throw new UnsupportedOperationException();
    }

    public boolean hasMoreElements() {
        return cellIterator.hasNext();
    }

    public ExcelCell nextElement() {
        return new ExcelCell(cellIterator.next());
    }
}
