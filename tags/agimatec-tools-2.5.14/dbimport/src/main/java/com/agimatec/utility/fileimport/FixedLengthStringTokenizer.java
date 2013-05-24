package com.agimatec.utility.fileimport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 11.09.2007 <br/>
 * Time: 19:20:27 <br/>
 * Copyright: Agimatec GmbH
 */
class FixedLengthStringTokenizer implements LineTokenizer<String, String> {
    private List myStringList = new ArrayList();
    private Iterator<String> myStringListIterator = null;

    /**
     * negative Werte in fixedLengths stehen für absolute Anzahl Zeichen, die zu ignoreren sind
     * @param aString
     * @param fixedLengths - fixed length of each column. when fixedLength < 0, the column
     * will be ignored by the tokenizer and has the length of abs(fixedLength)
     */
    public FixedLengthStringTokenizer(String aString, int[] fixedLengths) {
        //Prueft ob die Argumente nicht null oder leer sind
        if (aString == null || "".equals(aString))
            throw new IllegalArgumentException("String must not be null or empty!");
        if (fixedLengths == null || fixedLengths.length == 0)
            throw new IllegalArgumentException("fixedLengths must not be null or empty!");

        if (makeMyStringList(aString, fixedLengths)) {
            myStringListIterator = myStringList.iterator();
        }
    }

    private boolean makeMyStringList(String aString, int[] lengths) {
        int positionString = 0;
        for (int length : lengths) {
            int absLength = Math.abs(length);
            if (positionString + absLength > aString.length()) {
                throw new IllegalArgumentException(
                        "The cumulated length is longer than the length of the row!");
            }
            if (length > 0) { // length <= 0 ==> ignore
                String aktToken =
                        aString.substring(positionString, positionString + length);
                myStringList.add(aktToken.trim());
            }
            positionString += absLength;
        }
        return true;
    }

    public boolean hasMoreElements() {
        return myStringListIterator.hasNext();
    }

    public String nextElement() {
        return myStringListIterator.next();
    }

    /** this format does not support multi-line records as far as I know */
    public boolean isLineIncomplete() {
        return false;
    }

    public String continueParse(String aSingleValue, String aRecord) {
        throw new UnsupportedOperationException();
    }
}
