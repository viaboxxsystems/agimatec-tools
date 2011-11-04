package com.agimatec.commons.util;

/**
 * This class contains static methods that operate on Strings or return Strings.
 * Methods for conversion and formatting can be found here.
 */
public class StringUtils {

    public static String toSQLLiteral(String string) {
        if (string == null) return null;
        StringBuilder buf = new StringBuilder(string.length() + 3);
        appendSQLLiteral(string, buf);
        return buf.toString();
    }

    /**
     * double ' for usage as SQL literal.
     * replace \r and \n by chr(13) and chr(10)
     * append the given string as an SQL literal (embedded with ') or null if the given string is null
     */
    public static void appendSQLLiteral(String string, StringBuilder buf) {
        if (string == null) {
            buf.append("NULL");
            return;
        }
        final int length = string.length();
        byte bef = 0;
        for (int i = 0; i < length; i++) {
            char each = string.charAt(i);
            if (each == '\n' || each == '\r') {
                if (bef == 1) buf.append("\'||");
                else if (bef == 2) buf.append("||");
                if (each == '\n') buf.append("chr(10)");
                if (each == '\r') buf.append("chr(13)");
                bef = 2;
            } else {
                if (bef == 2) buf.append("||'");
                else if (bef == 0) buf.append('\'');
                // double '
                if (each == '\'') buf.append("''");
                else buf.append(each);
                bef = 1;
            }
        }
        if (bef == 1) buf.append('\'');
        else if (bef == 0) buf.append("''");
    }

}

