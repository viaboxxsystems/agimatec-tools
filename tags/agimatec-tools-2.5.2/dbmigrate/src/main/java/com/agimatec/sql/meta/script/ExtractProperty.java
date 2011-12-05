package com.agimatec.sql.meta.script;


/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
class ExtractProperty extends A_ExtractPart {
    private String startDelimeter, endDelimeter;
    private final String name;
    private boolean optional;
    private String word;

    public ExtractProperty(String aName) {
        name = aName;
    }

    /**
     * @param aProp      - name of the property to extract
     * @param aWord      - key token to fit with or null
     * @param isOptional - true (word must be != null, but in the string to parse this property is optional)
     */
    public ExtractProperty(String aProp, String aWord, boolean isOptional) {
        name = aProp;
        word = aWord;
        optional = isOptional;
    }


    public void setStartDelimeter(String startDelimeter) {
        this.startDelimeter = startDelimeter;
    }

    public void setEndDelimeter(String endDelimeter) {
        this.endDelimeter = endDelimeter;
    }

    public int fits(String aToken) {
        return optional ? C_NOT_HANDLED : C_ERROR;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (optional) buf.append('[');
        buf.append("${");
        buf.append(name);
        if (word != null) {
            buf.append("(");
            buf.append(word);
            buf.append(")");
        }
        if (startDelimeter != null) {
            buf.append('{').append(startDelimeter).append('}');
            if (endDelimeter != null && !endDelimeter.equals(startDelimeter)) {
                buf.append('{').append(endDelimeter).append('}');
            }
        }

        buf.append('}');
        if (optional) buf.append(']');
        return buf.toString();
    }

    public int process(String aToken, PropertiesExtractor extractor) {
        if (word == null) {
            if (startDelimeter == null) {
                setValue(extractor, aToken);
                return C_FIT;
            } else {
                if (aToken.startsWith(startDelimeter)) {
                    if (aToken.length() > startDelimeter.length() && aToken.endsWith(endDelimeter)) {
                        setValue(extractor, aToken.substring(startDelimeter.length(),
                                aToken.length() - endDelimeter.length()));
                        return C_FIT;
                    }
                    return C_MAY_FIT;
                }
            }
        } else {
            boolean needsMore = false, error = false;
            if (startDelimeter != null) {
                if (aToken.startsWith(startDelimeter)) {
                    if (aToken.endsWith(endDelimeter)) {
                        aToken = aToken.substring(startDelimeter.length(),
                                aToken.length() - endDelimeter.length());
                    } else {
                        aToken = aToken.substring(startDelimeter.length());
                        needsMore = true;
                    }
                } else {
                    error = true;
                }
            }
            if (!error) {
                if (!needsMore && aToken.equalsIgnoreCase(word)) {
                    setValue(extractor, aToken);
                    return C_FIT;
                } else if (word.toUpperCase().startsWith(aToken.toUpperCase())) {
                    return C_MAY_FIT;
                }
            }
        }
        return (optional) ? C_NOT_HANDLED :
                C_ERROR; // when optional and doesn't => return 3 so the next part can check it
    }

    private void setValue(PropertiesExtractor extractor, String aToken) {
        extractor.current.put(name, aToken);
    }

    protected boolean isOptional() {
        return optional;
    }
}
