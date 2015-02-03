package com.agimatec.utility.fileimport;

/**
 * Description: Factory that creates FixedLengthStringTokenizer <br/>
 * User: roman.stumm <br/>
 * Date: 11.09.2007 <br/>
 * Time: 19:30:59 <br/>
 */
public class FixedLengthStringTokenizerFactory implements LineTokenizerFactory<String, String>
{
    private int[] fixedLengths;

    public LineTokenizer<String, String> createTokenizer(String aLine) {
        return new FixedLengthStringTokenizer(aLine, fixedLengths);
    }

    public int[] getFixedLengths() {
        return fixedLengths;
    }

    public void setFixedLengths(int[] fixedLengths) {
        this.fixedLengths = fixedLengths;
    }

    public LineReader<String> createLineReader() {
        return new StringLineReader();
    }
}
