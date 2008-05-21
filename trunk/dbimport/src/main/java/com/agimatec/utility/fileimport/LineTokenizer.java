package com.agimatec.utility.fileimport;

import java.util.Enumeration;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:11:33 <br/>
 * Copyright: Agimatec GmbH
 */
public interface LineTokenizer extends Enumeration<String> {
    /**
     * @return true when this line is incomplete - set after parsing all tokens
     */
    boolean isLineIncomplete();

    String continueParse(Object aSingleValue, String aRecord);
}
