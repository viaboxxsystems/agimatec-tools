package com.agimatec.sql.meta.script;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * <b>Description:</b>   a class that can extract properties out of a string by using an expression tree.
 * This is a kind of string parser to extract some values, e.g. out of a SQL script or anything.<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public class PropertiesExtractor {
    private String delim;

    // state during extraction
    private String text;
    private HashMap properties; // root result map

    private RevertableStringTokenizer tokens;
    private String token;
    Map current;

    public PropertiesExtractor() {
        delim = ",() \t";
    }

    public PropertiesExtractor(String aDelim) {
        delim = aDelim;
    }

    public void close() {
        current = null;
        properties = null;
        token = null;
        tokens = null;
        text = null;
    }

    public Map extract(String aText, ExtractExpr aExpr) {
        text = aText;
        properties = new HashMap();
        tokens = new RevertableStringTokenizer(new StringTokenizer(text, delim, true));
        current = properties;
        token = null;
        int result = process(aExpr);
        return (result == A_ExtractPart.C_ERROR || result == A_ExtractPart.C_FIT_NOT) ?
                null : properties;
    }

    public Map getProperties() {
        return properties;
    }

    int process(ExtractExpr aExpr) {
        Iterator parts = aExpr.parts();
        int repeatResult = A_ExtractPart.C_NOT_HANDLED;
        do {
            if (repeatResult == A_ExtractPart.C_ERROR) return A_ExtractPart.C_ERROR;

            A_ExtractPart part = null;
            boolean concatMode = false;

            while ((token != null || tokens.hasMoreTokens()) &&
                    (parts.hasNext() || part != null)) {
                if (token == null) token = tokens.nextToken();
                if (concatMode) {
                    token += tokens.nextToken();
                }
                if (part == null) part = (A_ExtractPart) parts.next();
                boolean isDelim = (delim.indexOf(token) >= 0);
                if (isDelim) { // delimeter found
                    if (!token.equals(" ")) {
                        int result = part.fits(token);
                        switch (result) {
                            case A_ExtractPart.C_ERROR:
                            case A_ExtractPart.C_FIT_NOT:
                                return result; // string passt nicht zur expr
                            case A_ExtractPart.C_FIT:
                                part = null;
                                token = null;
                                break;
                            case A_ExtractPart.C_NOT_HANDLED:
                                isDelim = false; // handle as word
                                break;
                            default: // do nothing
                        }
                    } else token = null;
                }
                if (!isDelim) { // word found, or a delim should be handled like a word when it could not be handled otherwise
                    int result = part.process(token, this);
                    switch (result) {
                        case A_ExtractPart.C_ERROR:    // does not fit, stop processing!
                            // string passt nicht zur expr
                            return A_ExtractPart.C_ERROR;
                        case A_ExtractPart.C_FIT_NOT:
                        case A_ExtractPart.C_FIT:  // it fits, continue use next token and next part
                            part = null;
                            token = null;
                            concatMode = false;
                            break;
                        case A_ExtractPart.C_MAY_FIT:   // it may fit, keep&concat token and keep part
                            concatMode = true;
                            break;
                        case A_ExtractPart.C_NOT_HANDLED:   // may not fit, keep token and check with next part
                            part = null;
                            concatMode = false;
                            break;
                        default:
                            throw new IllegalStateException("internal error: " + result);
                    }
                }
            }
            if (parts.hasNext() || part !=
                    null) {   // there are parts left, check that these are optional
                if (part == null) part = (A_ExtractPart) parts.next();
                while (part != null) {
                    if (!part.isOptional()) {
                        /*throw new IllegalStateException("premature end at: " + token
               + " of text: '" + text + "' missing '"
               + part + "' in '" + aExpr + "'");*/
                        return A_ExtractPart.C_ERROR;
                    }
                    part = (parts.hasNext()) ? (A_ExtractPart) parts.next() : null;
                }
                return A_ExtractPart.C_FIT;
            }
            if (aExpr.isRepeating()) {
                parts = aExpr.parts();
                if (token != null && delim.indexOf(token) == -1) token =
                        null; // expect any delim (e.g. because previous part could be a OptionalProperty)
                do {
                    if (token == null && tokens.hasMoreTokens()) {
                        token = tokens.nextToken();
                    }
                    if (" ".equals(token)) token = null;
                } while (token == null && tokens.hasMoreTokens());
                repeatResult = aExpr.prepareLoop(token, this);
                if (repeatResult == A_ExtractPart.C_FIT) token = null;
            }
        } while (repeatResult == A_ExtractPart.C_FIT ||
                repeatResult == A_ExtractPart.C_MAY_FIT);

        if (repeatResult == A_ExtractPart.C_FIT_NOT)
            repeatResult = A_ExtractPart.C_NOT_HANDLED;
        return repeatResult;
    }

    protected RevertableStringTokenizer getTokens() {
        return tokens;
    }

    protected void setToken(String token) {
        this.token = token;
    }
}
