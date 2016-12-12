package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.LineReader;
import com.agimatec.utility.fileimport.LineTokenizer;
import com.agimatec.utility.fileimport.LineTokenizerFactory;

/**
 * Description: create reader/tokenizer for xls spreadsheets <br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:12:58 <br/>
 */
public class ExcelRowTokenizerFactory implements LineTokenizerFactory<ExcelRow, ExcelCell> {
    protected ExcelFormat format = ExcelFormat.HSSF;

    /**
     * @since 2.5.25
     */
    public ExcelFormat getFormat() {
        return format;
    }

    /**
     * @since 2.5.25
     */
    public void setFormat(ExcelFormat format) {
        this.format = format;
    }

    public LineTokenizer<ExcelRow, ExcelCell> createTokenizer(ExcelRow aLine) {
        return new ExcelRowTokenizer(aLine);
    }

    public LineReader<ExcelRow> createLineReader() {
        ExcelRowReader err = new ExcelRowReader();
        if (format != null) err.setFormat(format);
        return err;
    }
}
