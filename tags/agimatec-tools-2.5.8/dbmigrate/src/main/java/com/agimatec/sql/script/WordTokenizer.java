package com.agimatec.sql.script;

import java.io.IOException;
import java.io.Reader;

/**
 * <p>Title: Agimatec GmbH</p>
 * <p>Description: This class can be used like the java.util.StringTokenizer.
 * The main difference is, that the separator tokens are Strings not characters.</p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Agimatec GmbH </p>
 *
 * @author Roman Stumm
 */
public final class WordTokenizer {
    private boolean returnTokens;
    private boolean caseSensitive;
    private String[] separators;
    private final Reader myInput;

    private StringBuilder current;

    /**
     * full constructor.
     *
     * @param input            - a reader that reads the string to be parsed
     * @param separatorTokens  - array of separator strings
     * @param returnSeparators - true when nextToken() should return separators, false otherwise
     * @param aCaseSensitive   - true when separators are caseSensitive (faster), false otherwise (slower)
     */
    public WordTokenizer(Reader input, String[] separatorTokens, boolean returnSeparators, boolean aCaseSensitive) {
        myInput = input;
        separators = separatorTokens;
        returnTokens = returnSeparators;
        caseSensitive = aCaseSensitive;
    }

    /**
     * create a child!
     *
     * @param parent
     * @param separatorTokens
     * @param returnSeparators
     * @param aCaseSensitive
     */
    public WordTokenizer(WordTokenizer parent, String[] separatorTokens, boolean returnSeparators, boolean aCaseSensitive) {
        myInput = parent.getReader();
        separators = separatorTokens;
        returnTokens = returnSeparators;
        caseSensitive = aCaseSensitive;
        current = parent.current;
    }

    /**
     * convenience - caseSensitive = true
     *
     * @param input
     * @param separatorTokens
     * @param returnSeparators
     */
    public WordTokenizer(Reader input, String[] separatorTokens, boolean returnSeparators) {
        this(input, separatorTokens, returnSeparators, true);
    }

    public void continueFrom(WordTokenizer child) {
        current = child.current;
    }

    /**
     * convenience - returnSeparators = false, caseSensitive = true
     *
     * @param input
     * @param separatorTokens
     */
    public WordTokenizer(Reader input, String[] separatorTokens) {
        this(input, separatorTokens, false, true);
    }

    public void setSeparators(String[] aSeparators) {
        separators = aSeparators;
    }

    public void setCaseSensitive(boolean aCaseSensitive) {
        caseSensitive = aCaseSensitive;
    }

    public void setReturnTokens(boolean aReturnTokens) {
        returnTokens = aReturnTokens;
    }

    /**
     * @return null when there are no more tokens, the next token otherwise
     * @throws java.io.IOException
     */
    public String nextToken() throws IOException {
        Object next;
        do {
            next = nextObject();
        } while (next == this); // skip separators if not wanted
        return (String) next;
    }

    /**
     * @return null or the next element
     */
    private Object nextObject() throws IOException {
        String token = findToken();
        if (token != null) return popCurrent(token);
        int next = readNext();
        if (next != -1) {
            addChar(next);
            while ((token = findToken()) == null && next != -1) {
                next = readNext();
                addChar(next);
            }
        }
        return popCurrent(token);
    }

    protected int nextChar() throws IOException {
        if (current.length() > 0) {
            char r = current.charAt(0);
            current = new StringBuilder(current.substring(1));
            return r;  
        }
        else return readNext();
    }

    protected int readNext() throws IOException {
        return myInput.read();
    }

    public Reader getReader() {
        return myInput;
    }

    private String findToken() {
        String token = findPossibleToken();
        if (token != null) {
            String str = current.substring(current.length() - token.length() - 1);
            for (String each : separators) {
                if (startsWith(each, str)) {
                    return null;
                }
            }
            return token;
        }
        return null;
    }

    private String findPossibleToken() {
        if (current == null || current.length() == 0) return null;
        String str = current.substring(0, current.length() - 1);
        if (str.length() == 0) return null;
        for (String each : separators) {
            if (endsWith(str, each)) {
                return each;
            }
        }
        return null;
    }

    private String endsWithToken() {
        if (current == null) return null;
        String str = current.toString();
        if (str.length() == 0) return null;
        for (String each : separators) {
            if (endsWith(str, each)) {
                return each;
            }
        }
        return null;
    }

    protected void addChar(int next) {
        if (next != -1) {
            if (current == null) current = new StringBuilder();
            current.append((char) next);
        }
    }

    /**
     * return true if the token is one of the receiver's separator tokens
     *
     * @param token - Not null!
     * @return
     */
    public boolean isSeparator(String token) {
        for (String each : separators) {
            if (equals(token, each)) return true;
        }
        return false;
    }

    private boolean equals(String s1, String s2) {
        return (caseSensitive && s1.equals(s2)) ||
                (!caseSensitive && s1.equalsIgnoreCase(s2));
    }

    private boolean endsWith(String s1, String s2) {
        return (caseSensitive && s1.endsWith(s2)) ||
                (!caseSensitive && s1.toLowerCase().endsWith(s2.toLowerCase()));
    }

    private boolean startsWith(String s1, String s2) {
        return (caseSensitive && s1.startsWith(s2)) ||
                (!caseSensitive && s1.toLowerCase().startsWith(s2.toLowerCase()));
    }

    private int indexOf(String str, String token) {
        return caseSensitive ? str.indexOf(token) :
                str.toLowerCase().indexOf(token.toLowerCase());
    }

    private Object popCurrent(String token) {
        if (current == null) return null;
        final String str = current.toString();
        if (token != null) {
            if (equals(str, token)) {
                current = null;
                if (returnTokens) return str;
                else return this;  // return this to mark unwanted separator
            } else {
                int idx = indexOf(str, token);
                if (idx > 0) {
                    current = new StringBuilder(str.substring(idx));
                    return str.substring(0, idx);
                } else {
                    idx = token.length();
                    current = new StringBuilder(str.substring(idx));
                    if (returnTokens) return str.substring(0, idx);
                    else return this;  // return this to mark unwanted separator
                }
            }
        } else {
            token = endsWithToken();
            if (token != null) {
                return popCurrent(token); // recursion!
            } else {
                current = null;
                return str;
            }
        }
    }

}