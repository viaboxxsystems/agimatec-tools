package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.ImporterException;
import com.agimatec.utility.fileimport.LineReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description: ExcelRowTokenizerFactory that supports multiple imports on the same excel workbook.
 * This is useful if multiple sheets should be imported by keeping the workbook instance open
 * until it is explictly closed.<br>
 * <p>This class is not thread-safe!</p>
 * Example 1:
 * <pre>
 *     InputStream stream = new FileInputStream("workbook.xls");
 *     ExcelRowTokenizerFactory factory = new SharedExcelRowReaderFactory(stream, true);
 *     try {
 *        ImporterSpec spec1 = new ExcelImporterSpec("SheetName1") {
 *         protected void processRow(ExcelImportProcessor processor) throws ImporterException {
 *              processor.getString("column title 1"));
 *              processor.getBoolean("column title 2"));
 *           }
 *        };
 *        spec1.setLineTokenizerFactory(factory);
 *        Importer importer1 = new Importer(spec1);
 *        importer1.importFrom((InputStream) null);
 *
 *
 *        ImporterSpec spec2 = new ExcelImporterSpec("SheetName2") { ... };
 *        spec2.setLineTokenizerFactory(factory);
 *        Importer importer2 = new Importer(spec2);
 *        importer2.importFrom((InputStream) null);
 *
 *     } finally {
 *      stream.close();
 *     }
 * </pre>
 * or Example 2:
 * <pre>
 *     InputStream stream = new FileInputStream("workbook.xls");
 *     ExcelRowTokenizerFactory factory = new SharedExcelRowReaderFactory();
 *     try {
 *        ImporterSpec spec1 = new ExcelImporterSpec("SheetName1") { ... };
 *        spec1.setLineTokenizerFactory(factory);
 *        Importer importer1 = new Importer(spec1);
 *        importer1.importFrom(stream);
 *
 *
 *        ImporterSpec spec2 = new ExcelImporterSpec("SheetName2") { ... };
 *        spec2.setLineTokenizerFactory(factory);
 *        Importer importer2 = new Importer(spec2);
 *        importer2.importFrom(stream);
 *
 *     } finally {
 *      stream.close();
 *     }
 * </pre>
 * <p>
 * User: roman.stumm<br>
 * Date: 23.05.13<br>
 * Time: 14:58<br>
 * viaboxx GmbH, 2013
 * </p>
 *
 * @since 2.5.13
 */
public class SharedExcelRowReaderFactory extends ExcelRowTokenizerFactory {
    private InputStream stream;
    private final boolean keepOpen;
    private ExcelRowReader sharedReader;

    public SharedExcelRowReaderFactory() {
        keepOpen = true;
    }

    public SharedExcelRowReaderFactory(InputStream stream, boolean keepOpen) {
        this.stream = stream;
        this.keepOpen = keepOpen;
    }

    @Override
    public LineReader<ExcelRow> createLineReader() {
        if (sharedReader == null) {
            sharedReader = new ExcelRowReader(keepOpen);
        } else {
            sharedReader.setRowIterator(null);
        }
        if (stream != null) {
            try {
                sharedReader.setStream(stream);
            } catch (IOException e) {
                throw new ImporterException(e, true);
            }
        }
        return sharedReader;
    }
}
