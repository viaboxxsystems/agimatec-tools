package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * Description: Convenience ImporterSpec for importing Excel spreadsheets.
 * Subclass this and implement the method 'processRow(ExcelImportProcessor processor)' to handle the row.<br>
 * <p>
 * User: roman.stumm<br>
 * Date: 23.05.13<br>
 * Time: 15:54<br>
 * viaboxx GmbH, 2013
 * </p>
 *
 * @see SharedExcelRowReaderFactory Examples using this class with SharedExcelRowReaderFactory
 * @since 2.5.13
 */
public abstract class ExcelImporterSpec extends LineImporterSpecAutoFields {
    private final String sheetName;

    public ExcelImporterSpec(String sheetName) {
        this(sheetName, new SharedExcelRowReaderFactory());
    }

    public ExcelImporterSpec(String sheetName, LineTokenizerFactory<ExcelRow, ExcelCell> excelRowTokenizerFactory) {
        this.sheetName = sheetName;
        setLineTokenizerFactory(excelRowTokenizerFactory);
        setHeaderSpec(LineImporterSpec.Header.INDEX);
    }

    @Override
    public LineReader createLineReader(Closeable aReader) throws IOException {
        ExcelRowReader reader = (ExcelRowReader) super.createLineReader(aReader);
        reader.setSheetName(sheetName);
        return reader;
    }

    @Override
    public void processRow(LineImportProcessor processor) throws ImporterException {
        if (processor.getRowCount() > getHeaderLineIndex()) {  // skip all rows above headers
            processRow((ExcelImportProcessor) processor);
        }
    }

    protected abstract void processRow(ExcelImportProcessor processor);

    @Override
    public ImporterProcessor createProcessor(Importer importer) {
        return new ExcelImportProcessor(this, importer);
    }
}