package com.agimatec.sql.meta.script;

import com.agimatec.sql.script.WordTokenizer;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

/**
 * <b>Description:</b>   Create an extract expr from its toString() representation<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public class ExtractExprBuilder {
    private final String input;
    private final WordTokenizer tokens;
    private final static String[] words =
            {"${", "{", "[", "(", "'?'", "'", "}", "]", ")", "...", " "};
    private ExtractExpr expression;
    private ExtractExpr current;
    private String word;

    protected ExtractExprBuilder(String aFormatString) {
        input = aFormatString;
        tokens = new WordTokenizer(new StringReader(input), words, true, true);
    }

    // example for a format string:
    // {create-table CREATE TABLE ${table} '('
    // {columndefinition ${column} ${typeName}
    // [{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]...','} ')'}

    /**
     * API - parse the ExtractExpr.toString() representation to create an instance of ExtractExpr.
     *
     * @param format - a string in ExtractExpr.toString() format
     * @return a extractexpr
     * @throws ParseException if string format does not match (or if this class has a bug)
     */
    public static ExtractExpr buildExpr(String format) throws ParseException {
        ExtractExprBuilder builder;
        try {
            builder = new ExtractExprBuilder(format);
            builder.parse();
        } catch (IOException e) {
            throw new ParseException(format, 0);
        }
        return builder.getExpression();
    }

    private void parse() throws IOException, ParseException {
        nextWord();
        if (!isWord("{")) throw parseException();
        nextWord();
        expression = new ExtractExpr(word);
        current = expression;
        ExpBuildState state = new ExpBuildState();
        parseExpression(state);
        nextWord();
        if (word != null) throw parseException("premature end");
    }

    private void parseExpression(ExpBuildState state) throws IOException, ParseException {
        nextWord();
        while (word != null) {
            if (word == null) break;
            int idx = ArrayUtils.indexOf(words, word, 0);
            switch (idx) {
                // "${"
                case 0:
                    parseProperty(state);
                    break;
                    // "{"
                case 1:
                    parseSubExpression(state);
                    break;
                    // "["
                case 2:
                    state.setOptional(true);
                    break;
                    // "("
                case 3:
                    break;
                    // "'?'"
                case 4:
                    current.addSeparator();
                    break;
                    // "'"
                case 5:
                    current.addSeparator(parseSeparator());
                    break;
                    // "}"
                case 6:
                    return;
                    // "]"
                case 7:
                    state.setOptional(false);
                    break;
                    // ")"
                case 8:
                    throw parseException();
                    // "..."
                case 9:
                    parseRepeat();
                    break;
                    // " "
                case 10:
                    break;
                    // any word
                case-1:
                    if (state.isOptional()) current.addOptionalWord(word);
                    else current.addWord(word);
                    break;
                    // unknown token
                default:
                    throw new IllegalStateException(
                            "internal error, unknown token: " + word);
            }
            nextWord();
        }
    }

    private void parseRepeat() throws IOException, ParseException {
        nextWord();
        if (isWord("'")) {
            String sep = parseSeparator();
            current.setRepeatSep(new ExtractSeparator(sep));
        } else if (isWord("'?'")) {
            current.setRepeatSep(new ExtractSeparator());
        } else throw parseException("separator expected");
    }

    private void parseSubExpression(ExpBuildState state)
            throws IOException, ParseException {
        ExtractExpr formerExpr = current;
        ExtractExpr subExpr = new ExtractExpr(nextWord());
        current = subExpr;
        parseExpression(new ExpBuildState());  // recursion!
        if (state.isOptional()) {
            formerExpr.addOptionalExpr(subExpr);
        } else {
            formerExpr.addExpr(subExpr);
        }
        current = formerExpr;
    }

    private String nextWord() throws IOException {
        word = tokens.nextToken();
        return word;
    }

    private void parseProperty(ExpBuildState state) throws IOException, ParseException {
        String propName = nextWord();
        nextWord();
        String pword = null;
        if (isWord("(")) {
            pword = parseUntil(")");
            nextWord();
        }
        if (isWord("{")) {
            String startDelim = nextWord();
            String endDelim = startDelim;
            if (!"}".equals(nextWord())) throw parseException();
            if ("{".equals(nextWord())) {
                endDelim = nextWord();
                if (!"}".equals(nextWord())) throw parseException();
                nextWord();
            }
            current.addProperty(pword, propName, state.isOptional(), startDelim, endDelim);
        } else {
            if (!isWord("}")) throw parseException();
            if (state.isOptional()) {
                current.addOptionalProperty(pword, propName);
            } else {
                current.addProperty(pword, propName);
            }
        }
    }

    private boolean isWord(String s) {
        return s.equals(word);
    }

    private String parseSeparator() throws IOException, ParseException {
        return parseUntil("'");
    }

    private String parseUntil(String terminalToken) throws IOException, ParseException {
        StringBuilder sep = new StringBuilder();
        nextWord();
        while (!isWord(terminalToken) && word != null) {
            sep.append(word);
            nextWord();
        }
        if (word == null) throw parseException("premature end, missing " + terminalToken);
        return sep.toString();
    }

    private ParseException parseException() {
        return new ParseException(input + " at: " + word, 0);
    }

    private ParseException parseException(String s) {
        return new ParseException(s + " at: " + word, 0);
    }


    private ExtractExpr getExpression() {
        return expression;
    }
}

class ExpBuildState {
    boolean optional;

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean aOptional) {
        optional = aOptional;
    }
}

