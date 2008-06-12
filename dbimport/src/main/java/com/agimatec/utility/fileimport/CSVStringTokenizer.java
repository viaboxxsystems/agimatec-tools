package com.agimatec.utility.fileimport;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * <p>Description: Tokenizer that replies empty strings
 * when two separators are immediately in sequence and can handle "".
 * Useful for parsing a line in a .csv file.</p>
 * <p>Copyright: Agimatec GmbH, 2007</p>
 *
 * @author Roman Stumm
 */
class CSVStringTokenizer implements LineTokenizer<String, String> {
    private final String mySeparators;
    private final String myQuotaChar;
    private StringTokenizer myTokenizer;
    private boolean wasSeparator;

    /**
     * lineIncomplete = true
     * when the last token of the string is an incomplete quoted line that must be continued (when line is broken after \n)
     */
    private boolean lineIncomplete;

    /**
     * Constructor declaration.
     * Use '"' as Quota by default.
     *
     * @param object
     * @param separators - a string with separator chars (e.g. ";")
     */
    public CSVStringTokenizer(String object, String separators) {
        this(object, separators, "\"");
    }

    /**
     * @param object         - the string in csv format to be parsed
     * @param separators     - a string with separator chars (e.g. ";")
     * @param extraDelimeter - a single-char string with the quota char (e.g. "\"")
     */
    public CSVStringTokenizer(String object, String separators, String extraDelimeter) {
        mySeparators = separators;
        myQuotaChar = extraDelimeter;
        initTokenizer(object);
    }

    private void initTokenizer(String object) {
        lineIncomplete = wasSeparator = false;
        myTokenizer = new StringTokenizer(object,
                (myQuotaChar != null) ? mySeparators + myQuotaChar : mySeparators, true);
    }

    public boolean isLineIncomplete() {
        return lineIncomplete;
    }

    public String continueParse(String aSingleValue, String aRecord) {
        initTokenizer(aRecord);
        return aSingleValue + "\n" + parseQuoted();
    }

    public boolean hasMoreElements() {
        return (myTokenizer.hasMoreTokens() || wasSeparator);
    }

    public String nextElement() {
        String result;
        if (myTokenizer.hasMoreTokens()) {
            result = parseToken();
        } else if (wasSeparator) {
            wasSeparator = false;
            result = "";
        } else {
            throw new NoSuchElementException();
        }
        return result;
    }

    private boolean isSeparator(String each) {
        return (mySeparators.indexOf(each) >= 0);
    }

    private boolean isQuota(String each) {
        return (myQuotaChar != null && myQuotaChar.equals(each));
    }

    private String parseToken() {
        String each = myTokenizer.nextToken();
        if (isSeparator(each)) {
            return "";
        } else if (isQuota(each)) {
            wasSeparator = false;
            return parseQuoted();
        } else {
            wasSeparator = false;
            return parseNormal(each);
        }
    }

    private String parseNormal(String each) {
        StringBuilder buf = new StringBuilder(each);
        while (myTokenizer.hasMoreTokens()) {
            each = myTokenizer.nextToken();
            if (isSeparator(each)) {
                wasSeparator = true;
                break;
            } else {
                buf.append(each);
            }
        }
        return buf.toString();
    }

    private String parseQuoted() {
        StringBuilder buf = new StringBuilder();
        boolean maybeDoubleQuota = false;
        while (myTokenizer.hasMoreTokens()) {
            String each = myTokenizer.nextToken();
            if (isQuota(each)) {
                if (maybeDoubleQuota) {
                    buf.append(myQuotaChar);
                    maybeDoubleQuota = false;
                } else {
                    maybeDoubleQuota = true;
                }
            } else {
                if (maybeDoubleQuota) {
                    if (!isSeparator(each)) {
                        throw new IllegalArgumentException(
                                "separator expected, but found: " + each);
                    }
                    wasSeparator = true;
                    break;
                } else {
                    buf.append(each);
                }
            }
        }
        if (!maybeDoubleQuota) {
            lineIncomplete = true;
        }
        return buf.toString();
    }
}

