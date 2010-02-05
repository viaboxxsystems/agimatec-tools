package com.agimatec.utility.fileimport.spreadsheet;

import com.agimatec.utility.fileimport.Importer;
import com.agimatec.utility.fileimport.LineImporterSpec;
import com.agimatec.utility.fileimport.LineImporterSpecAutoFields;
import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 15:49:52 <br/>
 * Copyright: Agimatec GmbH
 */
public class ExcelImportTest extends TestCase {
    public ExcelImportTest(String s) {
        super(s);
    }

    public void testImportSpreadsheet() throws Exception {
        LineImporterSpecAutoFields spec = new LineImporterSpecAutoFields();
        spec.setHeaderSpec(LineImporterSpec.Header.INDEX);
        spec.setHeaderLineIndex(5);
        spec.setLineTokenizerFactory(new ExcelRowTokenizerFactory());
        Importer importer = new Importer(spec);
        importer.importFrom(getClass().getResourceAsStream("/Spreadsheet.xls"));        
    }

    public void testReadPOI() throws Exception {
        POIFSFileSystem fs =
                new POIFSFileSystem(getClass().getResourceAsStream("/Spreadsheet.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(2);
        HSSFCell cell = row.getCell((short) 3);
        if (cell == null) cell = row.createCell((short) 3);
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        cell.setCellValue(new HSSFRichTextString("a test"));

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    Map colorMapping = new HashMap();
    {
        colorMapping.put("SCHULUNG", null);
        colorMapping.put("PRESSE", null);
        colorMapping.put("Neu", null);
        colorMapping.put("Lager", null);
        colorMapping.put("Abbaustandort", null);
    }

    public void testIteratePOI() throws Exception {
        POIFSFileSystem fs =
                new POIFSFileSystem(getClass().getResourceAsStream("/Spreadsheet.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        int rowIdx = 0;
        for (Iterator<Row> rit = (Iterator<Row>) sheet.rowIterator(); rit.hasNext();) {
            Row row = rit.next();
            int columnIdx = 0;
            for (Iterator<Cell> cit = (Iterator<Cell>) row.cellIterator(); cit.hasNext();) {
                Cell cell = cit.next();
                // Do something here
                if(rowIdx == 1 || rowIdx == 2 || rowIdx == 3) {
                    scanColorMapping(cell);
                }
                String strValue = getStringValue(cell);
//                System.out.println("row: " + rowIdx + " col: " + columnIdx + " =\t" + strValue);
                columnIdx++;
            }
            rowIdx++;
        }
    }

    private void scanColorMapping(Cell cell) {
        String val = getStringValue(cell);
        if(val == null) return;
        if(colorMapping.containsKey(val)) {
            if(colorMapping.get(val) == null) {
                CellStyle style = cell.getCellStyle();
                short color = style.getFillForegroundColor();
                colorMapping.put(val, color);
                System.out.println(val + " ==> FFC " + style.getFillForegroundColor()) ;
            }
        }
    }

    private String getStringValue(Cell cell) {
        String strValue;
        switch(cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                strValue = String.valueOf(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                strValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                strValue = cell.getRichStringCellValue().getString();
                break;
            default:
                // do not handle Formular, Error, Blank, ...
                strValue = null;
        }
        return strValue;
    }
}
