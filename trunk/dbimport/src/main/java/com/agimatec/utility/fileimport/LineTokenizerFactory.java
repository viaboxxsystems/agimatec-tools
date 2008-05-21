package com.agimatec.utility.fileimport;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:31:35 <br/>
 * Copyright: Agimatec GmbH
 */
public interface LineTokenizerFactory {
    /**
     * create a line parser for the given line of data.
     * line parsers currently implement the Enumeration interface only.
     *
     * @param aLine
     * @return
     */
    LineTokenizer createTokenizer(String aLine);
}
