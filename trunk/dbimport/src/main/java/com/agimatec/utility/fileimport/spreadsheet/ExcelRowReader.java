package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.LineReader;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

/**
 * Description: read a spreadsheet line by line (each line is a {@Row})<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 18:03:51 <br/>
 * Copyright: Agimatec GmbH
 */
public class ExcelRowReader implements LineReader<ExcelRow> {
    private int sheetIndex = 0;
    private POIFSFileSystem fileSystem;
    private HSSFWorkbook workbook;
    private HSSFSheet sheet;
    private Iterator<Row> rowIterator;
    private InputStream stream;

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    /**
     * @throws UnsupportedOperationException - no ReaderInputStream support implemented yet!
     * @param aReader
     * @throws IOException
     */
    public void setReader(Reader aReader) throws IOException {
        throw new UnsupportedOperationException("InputStream required");
    }

    public void setStream(InputStream aReader) throws IOException {
        stream = aReader;
    }

    public ExcelRow readLine() throws IOException {
        init(); // lazy init to let the sheetIndex being set before creating the rowIterator
        if (rowIterator.hasNext()) {
            return new ExcelRow(rowIterator.next());
        } else {
            return null;
        }
    }

    protected void init() throws IOException {
        if(rowIterator != null) return; // short-circuit

        if (fileSystem == null) {
            fileSystem = new POIFSFileSystem(stream);
        }
        if (workbook == null) {
            workbook = new HSSFWorkbook(fileSystem);
        }
        if (sheet == null) {
            sheet = workbook.getSheetAt(sheetIndex);
        }
        rowIterator = sheet.rowIterator();
    }

    public void close() throws IOException {
        stream.close();
    }

    public POIFSFileSystem getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(POIFSFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public Iterator<Row> getRowIterator() {
        return rowIterator;
    }

    public void setRowIterator(Iterator<Row> rowIterator) {
        this.rowIterator = rowIterator;
    }

    public HSSFSheet getSheet() {
        return sheet;
    }

    public void setSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    public HSSFWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(HSSFWorkbook workbook) {
        this.workbook = workbook;
    }
}
