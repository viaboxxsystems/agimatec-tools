package com.agimatec.utility.fileimport.spreadsheet;


import com.agimatec.utility.fileimport.Importer;
import com.agimatec.utility.fileimport.LineImportProcessor;
import com.agimatec.utility.fileimport.LineImporterSpec;

import java.sql.Time;
import java.util.Date;
import java.util.Map;

/**
 * Description: The processor class used by the ExcelImporterSpec. It offers utility methods to
 * get cell values from ExcelCells. See ExcelImporterSpec for more information.<br>
 * <p>
 * User: roman.stumm<br>
 * Date: 23.05.13<br>
 * Time: 15:57<br>
 * viaboxx GmbH, 2013
 * </p>
 *
 * @see SharedExcelRowReaderFactory Examples using this class with SharedExcelRowReaderFactory
 * @since 2.5.13
 */
public class ExcelImportProcessor extends LineImportProcessor {
    public ExcelImportProcessor(LineImporterSpec spec,
                                Importer importer) {
        super(spec, importer);
    }

    public String getString(String colName) {
        @SuppressWarnings("unchecked")
        Map<String, ICell> row = getCurrentRow();
        return ExcelUtils.getString(row, colName);
    }

    public boolean getBoolean(String colName) {
        @SuppressWarnings("unchecked")
        Map<String, ICell> row = getCurrentRow();
        return ExcelUtils.getBoolean(row, colName);
    }

    public Integer getInteger(String colName) {
        @SuppressWarnings("unchecked")
        Map<String, ICell> row = getCurrentRow();
        return ExcelUtils.getInteger(row, colName);
    }

    public Long getLong(String colName) {
        @SuppressWarnings("unchecked")
        Map<String, ICell> row = getCurrentRow();
        return ExcelUtils.getLong(row, colName);
    }

    public Time getTime(String colName) {
        @SuppressWarnings("unchecked")
        Map<String, ICell> row = getCurrentRow();
        ICell cell = row.get(colName);
        if (cell == null) return null;
        Date date = cell.getDateValue();
        return date != null ? new Time(date.getTime()) : null;
    }

    public Double getDouble(String colName) {
        @SuppressWarnings("unchecked")
        Map<String, ICell> row = getCurrentRow();
        return ExcelUtils.getDouble(row, colName);
    }
}
