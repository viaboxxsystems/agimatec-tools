package com.agimatec.utility.fileimport;

/**
 * Description: create line tokenizers and line reader for a specific file format <br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:31:35 <br/>
 * Copyright: Agimatec GmbH
 */
public interface LineTokenizerFactory<Line, SingleValue> {
    /**
     * create a line parser for the given line of data.
     * line parsers currently implement the Enumeration interface only.
     */
    LineTokenizer<Line, SingleValue> createTokenizer(Line aLine);

    /** initialize the lineReader. */
    LineReader<Line> createLineReader();
}
