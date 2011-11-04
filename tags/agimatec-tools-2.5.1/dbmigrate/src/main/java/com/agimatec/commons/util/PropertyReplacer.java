package com.agimatec.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * <p>Title: Agimatec GmbH</p>
 * <p>Description: Class to replace ${property} properties by a value in a Map.
 * Idea and code based on Jakarta ANT's ProjectHelper.java.</p>
 * <p>Company: Agimatec GmbH </p>
 *
 * @author Roman Stumm
 */
public class PropertyReplacer {
    private static final Log myLogger = LogFactory.getLog(PropertyReplacer.class);

    private final Map myProperties;

    public PropertyReplacer(Map aProperties) {
        myProperties = aProperties;
    }

    public Map getProperties() {
        return myProperties;
    }

    /**
     * Replace in a simple style with flexible separator chars, e.g. [name] or {name} or (name}.
     * (Note: the double beginChar is replace by a single beginChar)
     *
     * @param value - string to replace properties in
     * @param chars - a two-char string with begin+end char, default (if null) = "{}"
     */
    public String replaceProperties(String value, String chars) {
        if (chars == null || chars.length() == 0) chars = "{}";
        final String beginChar = chars.substring(0, 1);
        final String endChar = chars.substring(1, 2);

        if (value == null || value.length() == 0) {
            return value;
        }

        final StringBuilder out = new StringBuilder();

        final StringTokenizer tokens =
                new StringTokenizer(value, beginChar + endChar, true);
        boolean propMode = false;

        while (tokens.hasMoreTokens()) {
            String tok = tokens.nextToken();
            if (tok.equals(beginChar)) {
                if (propMode) {
                    out.append(beginChar); // double {{ --> {
                }
                propMode = !propMode;
            } else if (tok.equals(endChar)) {
                if (!propMode) {
                    out.append(endChar); // } without { --> }
                } else {
                    propMode = false;
                }
            } else {
                if (propMode) {
                    if (getProperties().containsKey(tok)) {
                        out.append(getProperties().get(tok));
                    } else {
                        myLogger.warn("could not replace " + beginChar + tok + endChar +
                                " - unknown property");
                        out.append(beginChar).append(tok).append(endChar);
                    }
                } else {
                    out.append(tok);
                }
            }
        }

        return out.toString();
    }

    /**
     * Source based on ant's tools.ant.ProjectHelper.java
     * <p/>
     * Replace ${} style constructions in the given value with the string value of
     * the corresponding data types.
     *
     * @param value the string to be scanned for property references.
     * @return the string with properties replaced by their value
     */
    public String replaceProperties(String value) {
        if (value == null) {
            return null;
        }

        List fragments = new ArrayList();
        List propertyRefs = new ArrayList();
        parsePropertyString(value, fragments, propertyRefs);

        StringBuilder sb = new StringBuilder();
        Iterator i = fragments.iterator();
        Iterator j = propertyRefs.iterator();
        while (i.hasNext()) {
            Object fragment = i.next();
            if (fragment == null) {
                String propertyName = (String) j.next();
                fragment = getProperties().get(propertyName);

                if (fragment == null && myLogger.isWarnEnabled()) {
                    myLogger.warn("Property ${" + propertyName + "} has not been set");
                }

                if (fragment == null) {
                    fragment = "${" + propertyName + '}';
                }
            }

            sb.append(fragment);
        }

        return sb.toString();
    }

    /**
     * Source based on ant's ProjectHelper.java.
     * <p/>
     * This method will parse a string containing ${value} style
     * property values into two lists. The first list is a collection
     * of text fragments, while the other is a set of string property names
     * null entries in the first list indicate a property reference from the
     * second list.
     */
    private void parsePropertyString(String value, List fragments, List propertyRefs) {
        int prev = 0;
        int pos;
        while ((pos = value.indexOf("$", prev)) >= 0) {
            if (pos > 0) {
                fragments.add(value.substring(prev, pos));
            }

            if (pos == (value.length() - 1)) {
                fragments.add("$");
                prev = pos + 1;
            } else if (value.charAt(pos + 1) != '{') {
                fragments.add(value.substring(pos, pos + 2));
                prev = pos + 2;
            } else {
                int endName = value.indexOf('}', pos);
                if (endName < 0) {
                    throw new RuntimeException("Syntax error in property: " + value);
                }
                String propertyName = value.substring(pos + 2, endName);
                fragments.add(null);
                propertyRefs.add(propertyName);
                prev = endName + 1;
            }
        }

        if (prev < value.length()) {
            fragments.add(value.substring(prev));
        }
    }
}

