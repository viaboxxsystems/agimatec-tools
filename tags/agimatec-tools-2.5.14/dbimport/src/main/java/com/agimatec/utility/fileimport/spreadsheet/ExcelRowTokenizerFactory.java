package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.LineReader;
import com.agimatec.utility.fileimport.LineTokenizer;
import com.agimatec.utility.fileimport.LineTokenizerFactory;

/**
 * Description: create reader/tokenizer for xls spreadsheets <br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:12:58 <br/>
 * Copyright: Agimatec GmbH
 */
public class ExcelRowTokenizerFactory implements LineTokenizerFactory<ExcelRow, ExcelCell> {

    public LineTokenizer<ExcelRow, ExcelCell> createTokenizer(ExcelRow aLine) {
        return new ExcelRowTokenizer(aLine);
    }

    public LineReader<ExcelRow> createLineReader() {
        return new ExcelRowReader();
    }
}
