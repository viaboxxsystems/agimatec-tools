package com.agimatec.sql.meta.script;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
class ExtractSeparator extends A_ExtractPart {
    private final String separator;

    /**
     * @param aSeparator - "," or "(" or ")"
     */
    public ExtractSeparator(String aSeparator) {
        separator = aSeparator;
    }

    public ExtractSeparator() {
        separator = null;
    }

    public boolean isAnySep() {
        return separator == null;
    }

    public int process(String aToken, PropertiesExtractor extractor) {
        return C_ERROR;
    }

    /**
     * C_FIT_NOT = -1; // NOT OK, use next part, next token
     * C_ERROR = 0;  // NOT OK, error. stop!
     * C_FIT = 1; // OK, use next part, next token
     * C_MAY_FIT = 2; // OK, use next park, keep (or concat) token
     * C_NOT_HANDLED = 3; // UNKNOWN, use next part, keep token
     *
     * @param aToken
     * @return
     */
    public int fits(String aToken) {
        if (separator == null) {
            return C_MAY_FIT;
        } else if (separator.equals(aToken)) {
            return C_FIT;
        } else {
            return C_FIT_NOT;
        }
    }

    public String toString() {
        return separator == null ? "'?'" : "'" + separator + "'";
    }
}
