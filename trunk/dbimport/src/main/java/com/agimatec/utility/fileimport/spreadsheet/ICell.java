package com.agimatec.utility.fileimport.spreadsheet;

import java.util.Date;

/**
 * Description: Abstraction for a single spreadsheet cell <br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:21:10 <br/>
 * Copyright: Agimatec GmbH
 */
public interface ICell {
    /**
     * works with every cell data type.
     * @return adequate value type (Date, Boolean, Double, String, Byte (error))
     */
    Object getValue();

    /**
     * @return cell value as numeric (0/1 for true/false)
     * @throws NumberFormatException when value format is not a number
     */
    double getNumericValue();

    /**
     * works with every cell data type.
     * @return cell value as a string (or null, date-formatted string possible).
     */
    String getStringValue();

    /**
     * @return a date or null when cell type is not date
     */
    Date getDateValue();

    /**
     * @return null or cell comment
     */
    String getComment();

    /**
     * logical cell number in row
     * @return
     */
    int getColumnIndex();
}