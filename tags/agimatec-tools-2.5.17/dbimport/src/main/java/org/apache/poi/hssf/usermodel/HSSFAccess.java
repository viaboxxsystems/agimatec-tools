package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.record.RowRecord;

/**
 * Description: <br>
 * <p>
 * User: roman.stumm<br>
 * Date: 09.01.13<br>
 * Time: 14:06<br>
 * viaboxx GmbH, 2012
 * </p>
 */
public class HSSFAccess {
    public static InternalSheet getInternalSheet(HSSFSheet sheet) {
        return sheet.getSheet();
    }

    public static RowRecord getRowRecord(HSSFRow row) {
        return row.getRowRecord();
    }
}
