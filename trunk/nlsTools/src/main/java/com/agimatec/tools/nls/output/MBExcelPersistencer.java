package com.agimatec.tools.nls.output;

import com.agimatec.tools.nls.BundleWriterExcel;
import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: Load from and save bundles to an excel file <br>
 * User: roman.stumm<br>
 * Date: 29.12.2010<br>
 * Time: 14:44:37<br>
 * viaboxx GmbH, 2010
 */
public class MBExcelPersistencer extends MBPersistencer {
    private static final int STYLE_BOLD = 1;
    private static final int STYLE_ITALIC = 2;
    private static final int STYLE_REVIEW = 3;

    private HSSFWorkbook wb;
    private HSSFSheet sheet;
    private int rowNum = 0;
    private final Map<Integer, CellStyle> styles = new HashMap();
    private BundleWriterExcel bundleWriter;

    public MBExcelPersistencer() {
    }

    private void initStyles(HSSFWorkbook wb) {
        // cache styles used to write text into cells
        HSSFCellStyle style = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        styles.put(STYLE_BOLD, style);

        style = wb.createCellStyle();
        font = wb.createFont();
        font.setItalic(true);
        style.setFont(font);
        styles.put(STYLE_ITALIC, style);


        style = wb.createCellStyle();
        font = wb.createFont();
        font.setItalic(true);
        font.setColor(Font.COLOR_RED);
        style.setFont(font);
        styles.put(STYLE_REVIEW, style);
    }

    private int writeHeaders(MBBundle bundle) throws IOException {
        HSSFRow headerRow = createRow();
        HSSFCell cell = headerRow.createCell(0);
        cell.setCellStyle(styles.get(STYLE_BOLD));
        cell.setCellValue("Bundle:");

        cell = headerRow.createCell(1);
        cell.setCellStyle(styles.get(STYLE_BOLD));
        cell.setCellValue(bundle.getBaseName());

        headerRow = createRow();
        if (null != bundle.getInterfaceName()) {
            cell = headerRow.createCell(0);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue("Interface:");

            cell = headerRow.createCell(1);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue(bundle.getInterfaceName());
        }

        if (null != bundle.getSqldomain()) {
            cell = headerRow.createCell(2);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue("SQLDomain:");

            cell = headerRow.createCell(3);
            cell.setCellStyle(styles.get(STYLE_ITALIC));
            cell.setCellValue(bundle.getSqldomain());
        }

        rowNum++; // empty row
        headerRow = createRow();
        String[] headerCols = {"Key", "Description"};
        for (int i = 0; i < headerCols.length; i++) {
            HSSFCell headerCell = headerRow.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headerCols[i]);
            headerCell.setCellStyle(styles.get(STYLE_BOLD));
            headerCell.setCellValue(text);
        }
        int colNum = headerCols.length;
        int firstCol = colNum;

        List<String> locales = bundleWriter.getLocalesUsed();
        for (String each : locales) {
            HSSFCell headerCell = headerRow.createCell(colNum++);
            HSSFRichTextString text = new HSSFRichTextString(each);
            headerCell.setCellStyle(styles.get(STYLE_BOLD));
            headerCell.setCellValue(text);
        }
        return firstCol;
    }

    private void writeRows(MBBundle bundle, int firstCol) throws IOException {
        List<String> locales = bundleWriter.getLocalesUsed();
        for (MBEntry entry : bundle.getEntries()) {
            HSSFRow row = createRow();

            HSSFCell cell = row.createCell(0);
            cell.setCellValue(new HSSFRichTextString(entry.getKey()));

            if (entry.getDescription() != null) {
                cell = row.createCell(1);
                cell.setCellValue(new HSSFRichTextString(entry.getDescription()));
            }

            int colNum = firstCol;
            for (String each : locales) {
                MBText text = entry.getText(each);
                if (text != null) {
                    cell = row.createCell(colNum);
                    cell.setCellValue(new HSSFRichTextString(text.getValue()));
                    if (text.isReview()) {
                        cell.setCellStyle(styles.get(STYLE_REVIEW));
                    }
                }
                colNum++;
            }
        }
    }

    private HSSFRow createRow() {
        HSSFRow row = sheet.createRow(rowNum);
        rowNum++;
        return row;
    }

    public void save(MBBundles obj, File target) throws IOException {
        OutputStream out = new FileOutputStream(target);
        try {
            wb = new HSSFWorkbook();
            initStyles(wb);
            for (MBBundle bundle : obj.getBundles()) {
                bundleWriter = new BundleWriterExcel(bundle);
                sheet = wb.createSheet(bundle.getBaseName());
                writeRows(bundle, writeHeaders(bundle));
            }
            wb.write(out);
        } finally {
            rowNum = 0;
            out.close();
        }
    }

    public MBBundles load(File source) throws IOException, ClassNotFoundException {
        InputStream in = new FileInputStream(source);
        try {
            wb = new HSSFWorkbook(in);
            MBBundles bundles = new MBBundles();
            int sheetIdx = 0;
            sheet = wb.getSheetAt(sheetIdx++);
            while (sheet != null) {
                MBBundle bundle = new MBBundle();
                if (readSheet(bundle)) {
                    bundles.getBundles().add(bundle);
                }
                if (wb.getNumberOfSheets() > sheetIdx) {
                    sheet = wb.getSheetAt(sheetIdx++);
                } else {
                    sheet = null;
                }
            }
            return bundles;
        } finally {
            in.close();
            rowNum = 0;
        }
    }

    private boolean readSheet(MBBundle bundle) {
        if (sheet.getLastRowNum() == 0) return false;

        HSSFRow row = sheet.getRow(0);
        if (row.getLastCellNum() < 1 || row.getCell(1) == null) return false;
        bundle.setBaseName(row.getCell(1).getStringCellValue());

        row = sheet.getRow(1);
        if (row != null) {
            if (row.getCell(1) != null) {
                bundle.setInterfaceName(row.getCell(1).getStringCellValue());
            }

            if (row.getCell(3) != null) {
                bundle.setSqldomain(row.getCell(3).getStringCellValue());
            }
        }
        int firstCol = 2;

        rowNum = 3;
        row = sheet.getRow(rowNum++); // read locales
        int colNum = firstCol;

        List<String> locales = new ArrayList();

        HSSFCell cell = row.getCell(colNum++);
        while (colNum <= row.getLastCellNum()) {
            if (cell != null) {
                locales.add(cell.getStringCellValue());
            }
            if (row.getLastCellNum() >= colNum) {
                cell = row.getCell(colNum++);
            } else {
                cell = null;
            }
        }

        row = sheet.getRow(rowNum++);
        while (row != null) {
            MBEntry entry = new MBEntry();
            bundle.getEntries().add(entry);
            entry.setKey(row.getCell(0).getStringCellValue());
            if (row.getCell(1) != null) {
                entry.setDescription(row.getCell(1).getStringCellValue());
            }
            colNum = firstCol;
            for (String each : locales) {
                cell = row.getCell(colNum++);
                if (cell != null) {
                    MBText text = new MBText();
                    text.setLocale(each);
                    text.setValue(cell.getStringCellValue());
                    text.setReview(cell.getCellStyle().getFont(wb).getColor() == Font.COLOR_RED);
                    entry.getTexts().add(text);
                }
            }
            row = sheet.getRow(rowNum++);
        }
        return true;
    }
}
