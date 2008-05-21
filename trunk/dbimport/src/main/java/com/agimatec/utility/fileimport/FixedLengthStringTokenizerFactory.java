package com.agimatec.utility.fileimport;

/**
 * Description: Factory that creates FixedLengthStringTokenizer <br/>
 * User: roman.stumm <br/>
 * Date: 11.09.2007 <br/>
 * Time: 19:30:59 <br/>
 * Copyright: Agimatec GmbH
 */
public class FixedLengthStringTokenizerFactory implements LineTokenizerFactory
{
    private int[] fixedLengths;

    public LineTokenizer createTokenizer(String aLine) {
        return new FixedLengthStringTokenizer(aLine, fixedLengths);
    }

    public int[] getFixedLengths() {
        return fixedLengths;
    }

    public void setFixedLengths(int[] fixedLengths) {
        this.fixedLengths = fixedLengths;
    }
}
