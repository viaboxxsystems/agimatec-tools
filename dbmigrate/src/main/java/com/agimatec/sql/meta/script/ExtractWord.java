package com.agimatec.sql.meta.script;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 *
 * @author Roman Stumm
 */
class ExtractWord extends A_ExtractPart {
    private final String word;
    private final boolean optional;

    public ExtractWord(String aWord, boolean isOptional) {
        word = aWord;
        optional = isOptional;
    }

    public int fits(String aToken) {
        return word.equalsIgnoreCase(aToken) ? C_FIT : optional ? C_NOT_HANDLED : C_ERROR;
    }

    public String toString() {
        return (optional) ? "[" + word + "]" : word;
    }

    public int process(String aToken, PropertiesExtractor extractor) {
        return fits(aToken);
    }

    protected boolean isOptional() {
        return optional;
    }
}

