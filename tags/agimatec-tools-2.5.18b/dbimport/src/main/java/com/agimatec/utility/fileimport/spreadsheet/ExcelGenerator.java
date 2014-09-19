package com.agimatec.utility.fileimport.spreadsheet;


import com.agimatec.utility.fileimport.ImporterException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: convenience class to write data as excel worksheet. subclass to customize.
 * <br>
 * <pre>
 *         new MyExcelGenerator(inputData).generateWorkbook().write(outputStream);
 * </pre>
 * <p>
 * User: roman.stumm<br>
 * Date: 24.05.13<br>
 * Time: 11:17<br>
 * viaboxx GmbH, 2013
 * </p>
 *
 * @since 2.5.13
 */
public abstract class ExcelGenerator {
    protected HSSFWorkbook wb;
    protected Styles styles;

    public HSSFWorkbook generateWorkbook() {
        initWorkbook();
        initStyles();
        generateSheets();
        return wb;
    }

    /**
     * override in subclasses to generate the sheets into 'wb'
     */
    protected abstract void generateSheets();

    protected void initWorkbook() {
        wb = new HSSFWorkbook();
    }

    /**
     * method can be overwritten to create additional styles
     */
    protected void initStyles() {
        styles = new Styles();
    }

    public HSSFWorkbook getWb() {
        return wb;
    }

    public Styles getStyles() {
        return styles;
    }

    protected CellStyle style(String name) {
        return getStyles().get(name);
    }

    protected HSSFCell createCell(HSSFRow row, int column, Object value, CellStyle style) {
        HSSFCell cell = createCell(row, column, value);
        if (style != null)
            cell.setCellStyle(style);
        return cell;
    }

    protected HSSFCell createCell(HSSFRow row, int column, Object value) {
        HSSFCell cell = row.createCell(column);
        if (value instanceof String)
            cell.setCellValue((String) value);
        else if (value instanceof Enum)
            cell.setCellValue(((Enum) value).name());
        else if (value instanceof Calendar)
            cell.setCellValue((Calendar) value);
        else if (value instanceof Date)
            cell.setCellValue((Date) value); // do not forget to set a cellStyle with a dataFormat
        else if (value instanceof Number)
            cell.setCellValue(((Number) value).doubleValue());
        else if (value instanceof Boolean)
            cell.setCellValue((Boolean) value);
        else if (value instanceof RichTextString) {
            cell.setCellValue((RichTextString) value);
        } else if (value != null)
            throw new RuntimeException("Unsupported cell value " + value);
        return cell;
    }

    protected void createHeaders(HSSFRow row, int column, CellStyle style, String... headers) {
        int i = 0;
        for (String header : headers) {
            createCell(row, column + i, header, style);
            i++;
        }
    }

    protected static final String STYLE_boldTitle = "boldTitle";
    protected static final String STYLE_boldHeader = "boldHeader";
    protected static final String STYLE_redCell = "redCell";
    protected static final String STYLE_blueCell = "blueCell";

    /**
     * style factory class. replace by own class or add additional styles by overriding method 'initStyles()'
     */
    public class Styles {
        protected Map<String, CellStyle> styles = new ConcurrentHashMap<String, CellStyle>();

        /**
         * @param name
         * @return the style with given name. if not already contained in 'styles' map, invoke
         *         the public method in the receiver class to create and cache the style.
         */
        public CellStyle get(String name) {
            if (!styles.containsKey(name)) {
                try {
                    CellStyle style = (CellStyle) getClass().getMethod(name).invoke(this);
                    put(name, style);
                    return style;
                } catch (Exception e) {
                    throw new ImporterException("Cannot create style " + name, e, true);
                }
            }
            return styles.get(name);
        }

        public void put(String name, CellStyle style) {
            styles.put(name, style);
        }

        public void clean() {
            styles.clear();
        }
        /*
          STYLE METHODS: must return a CellStyle, must be public, must be no-arg methods.
         */

        public HSSFCellStyle boldTitle() {
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setFontHeightInPoints((short) 14);
            style.setFont(font);
            return style;
        }

        public HSSFCellStyle boldHeader() {
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            return style;
        }

        public HSSFCellStyle redCell() {
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setColor(IndexedColors.RED.getIndex());
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            return style;
        }

        public HSSFCellStyle blueCell() {
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setColor(IndexedColors.BLUE.getIndex());
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            return style;
        }
    }
}
