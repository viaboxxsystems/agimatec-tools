package com.agimatec.dbtransform.ejb3;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 03.07.2007 <br/>
 * Time: 16:22:52 <br/>
 */
public abstract class Ejb3Prototype implements Serializable {
    protected String toProperEntityName(String tableName) {
        return toProperName(tableName, true);
    }

    protected String toProperAttributeName(String columnName) {
        return toProperName(columnName, false);
    }

    protected String toPlural(String singular) {
        if (singular.endsWith("x") || singular.endsWith("s")) {
            return singular + "es";
        } else if(singular.endsWith("y")) {
            return singular.substring(0, singular.length()-1) + "ies";
        } else {
            return singular + "s";
        }
    }

    private String toProperName(String input, boolean upperNext) {
        StringBuilder buf = new StringBuilder(input.length());
        StringTokenizer tokens = new StringTokenizer(input, "_", true);
        while (tokens.hasMoreTokens()) {
            String tok = tokens.nextToken();
            if (tok.equals("_")) {
                upperNext = true;
                continue;
            }
            if (upperNext) {
                if (Character.isLowerCase(tok.charAt(0))) {
                    buf.append(Character.toUpperCase(tok.charAt(0)));
                    buf.append(tok.substring(1));
                } else {
                    buf.append(tok);
                }
                upperNext = false;
            } else {
                if (Character.isUpperCase(tok.charAt(0))) {
                    buf.append(Character.toLowerCase(tok.charAt(0)));
                    buf.append(tok.substring(1));
                } else {
                    buf.append(tok);
                }
            }
        }
        return buf.toString();
    }


}
