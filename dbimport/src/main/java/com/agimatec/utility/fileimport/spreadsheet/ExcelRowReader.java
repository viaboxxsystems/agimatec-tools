package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.ImporterException;
import com.agimatec.utility.fileimport.LineImportProcessor;
import com.agimatec.utility.fileimport.LineReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

/**
 * Description: read a spreadsheet line by line (each line is a {@Row})<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008, 24.05.2013<br/>
 * Time: 18:03:51 <br/>
 * Copyright: Viaboxx GmbH
 */
public class ExcelRowReader implements LineReader<ExcelRow> {
    protected ExcelFormat format = ExcelFormat.HSSF;
    private int sheetIndex = 0;
    private Workbook workbook;
    private Sheet sheet;
    private Iterator<Row> rowIterator;
    private InputStream stream;
    /**
     * @since 2.5.13
     */
    private String sheetName;
    /**
     * @since 2.5.13
     */
    private final boolean keepOpen;

    /**
     * @param keepOpen - true to prevent stream.close() on call of close()
     * @since 2.5.13
     */
    public ExcelRowReader(boolean keepOpen) {
        this.keepOpen = keepOpen;
    }

    /**
     * create an instance that closes the stream on call of close() (keepOpen = false)
     */
    public ExcelRowReader() {
        keepOpen = false;
    }

    /**
     * set sheet name to import. if sheetName is set, the sheetIndex will be ignored.
     *
     * @param sheetName
     * @since 2.5.13
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
        setSheet(null);
    }

    /**
     * @since 2.5.13
     */
    public String getSheetName() {
        return sheetName;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    /**
     * @return true when stream not closed when close() called on receiver.
     * @since 2.5.13
     */
    public boolean isKeepOpen() {
        return keepOpen;
    }

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

    /**
     * sheet index is 0-based. sheetName has priority before sheetIndex.
     *
     * @param sheetIndex
     */
    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
        setSheet(null);
    }

    /**
     * @param aReader
     * @throws UnsupportedOperationException - no ReaderInputStream support implemented yet!
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

    /**
     * @throws IOException
     * @throws ImporterException - invalid sheet name (sheet not found by name)
     */
    protected void init() throws IOException, ImporterException {
        if (rowIterator != null) return; // short-circuit

        if (workbook == null) {
            if(format == null) {
                workbook = new HSSFWorkbook(stream);
            } else {
                switch(format) {
                    case XSSF:
                        workbook = new XSSFWorkbook(stream);
                        break;
                    case SXSSF:
                        workbook = new SXSSFWorkbook(new XSSFWorkbook(stream));
                        break;
                    case HSSF:
                        workbook = new HSSFWorkbook(stream);
                }
            }
        }
        if (sheet == null) {
            if (sheetName != null) {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new ImporterException("Sheet not found by name: " + getSheetName(), true);
                }
            } else {
                sheet = workbook.getSheetAt(sheetIndex);
            }
        }
        rowIterator = sheet.rowIterator();
    }

    /**
     * close underlying stream (if keepOpen is false)
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (!keepOpen && stream != null) stream.close();
    }

    public Iterator<Row> getRowIterator() {
        return rowIterator;
    }

    public void setRowIterator(Iterator<Row> rowIterator) {
        this.rowIterator = rowIterator;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
        setRowIterator(null);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
        setSheet(null);
    }

    public InputStream getStream() {
        return stream;
    }

    /*  this does not work yet.
    public void removeCurrentRow(LineImportProcessor processor) {
        ExcelRow row = (ExcelRow) processor.getCurrentLine();
        int rowNum = row.getRowNum();
        sheet.removeRow(row.getRow());
        rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row theRow = rowIterator.next();
            if (theRow.getRowNum() == rowNum) {
                processor.setCurrentLine(theRow);
            }
        }
    }*/

    public void removeCurrentRow(LineImportProcessor processor) {
        // A HACK, I know. Prevent from ConcurrentModificationException but remove row from sheet:
        rowIterator.remove(); // remove the row that has just been read: from iterator..
        ExcelRow line = (ExcelRow) processor.getCurrentLine();
//        if(sheet instanceof HSSFSheet) {
//            HSSFAccess.getInternalSheet((HSSFSheet)sheet)
//                .removeRow(HSSFAccess.getRowRecord(((HSSFRow) line.getRow()))); // .. and from physical sheet
//        } else {
        sheet.removeRow(line.getRow());
//        }
    }

    // source from http://pastebin.com/ff806298
    // unsolved issues:
    // + What to do with the formula in the moved columns?
    // + Column breaks
    // + Merged regions
    public void removeColumn(int columnNum) {
        int maxColumn = 0;
        for (int r = 0; r < sheet.getLastRowNum() + 1; r++) {
            Row row = sheet.getRow(r);

            // if no row exists here; then nothing to do; next!
            if (row == null)
                continue;

            // if the row doesn't have this many columns then we are good; next!
            int lastColumn = row.getLastCellNum();
            if (lastColumn > maxColumn)
                maxColumn = lastColumn;

            if (lastColumn < columnNum)
                continue;

            for (int x = columnNum + 1; x < lastColumn + 1; x++) {
                Cell oldCell = row.getCell(x - 1);
                if (oldCell != null)
                    row.removeCell(oldCell);

                Cell nextCell = row.getCell(x);
                if (nextCell != null) {
                    Cell newCell = row.createCell(x - 1, nextCell.getCellTypeEnum());
                    cloneCell(newCell, nextCell);
                }
            }
        }

        // Adjust the column widths
        for (int c = 0; c < maxColumn; c++) {
            sheet.setColumnWidth(c, sheet.getColumnWidth(c + 1));
        }
    }

    /*
     * Takes an existing Cell and merges all the styles and forumla
     * into the new one
     */
    private static void cloneCell(Cell cNew, Cell cOld) {
        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());

        switch (cNew.getCellTypeEnum()) {
            case BOOLEAN:
                cNew.setCellValue(cOld.getBooleanCellValue());
                break;

            case NUMERIC:
                cNew.setCellValue(cOld.getNumericCellValue());
                break;

            case STRING:
                cNew.setCellValue(cOld.getStringCellValue());
                break;

            case ERROR:
                cNew.setCellValue(cOld.getErrorCellValue());
                break;

            case FORMULA:
                cNew.setCellFormula(cOld.getCellFormula());
                break;
        }

    }
}
