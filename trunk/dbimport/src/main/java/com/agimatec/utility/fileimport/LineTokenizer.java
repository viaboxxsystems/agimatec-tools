package com.agimatec.utility.fileimport;

import java.util.Enumeration;

/**
 * Description: responsible to read the data in a single line/row/record
 * in enumeration-style <br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:11:33 <br/>
 * Copyright: Agimatec GmbH
 */
public interface LineTokenizer<Line, Single> extends Enumeration<Single> {
    /**
     * @deprecated should be handled internally (only csv files)
     * @return true when this line is incomplete - set after parsing all tokens
     */
    boolean isLineIncomplete();

    /**
     * @deprecated should be handled internally (only csv files)
     * @param aSingleValue
     * @param anotherRecord
     */
    Single continueParse(Single aSingleValue, Line anotherRecord);
}
