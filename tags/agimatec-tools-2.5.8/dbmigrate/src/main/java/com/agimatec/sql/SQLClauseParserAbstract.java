/*--- formatted by Jindent 2.1, (www.c-lab.com/~jindent) ---*/

package com.agimatec.sql;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;

/**
 * Internal -
 * This class implements an abstract script to extract parameter
 * markers and names from an SQL clause.
 * The output (SQL-string) will be written to the given Writer (or SQLWriter).
 */
public abstract class SQLClauseParserAbstract {
    private final Writer resultWriter;
    protected final ReadStream inputStream;
    private boolean expressionMode;
    protected int position;
    protected static final char INPUT_PARAM_INDICATOR = '?';
    protected static final char INPUT_TEXT_INDICATOR = '\'';
    protected static final char INPUT_BRACKET_OPEN = '(';
    protected static final char INPUT_BRACKET_CLOSE = ')';
    protected static final String RESULT_PARAM_INDICATOR = "?";

    /**
     * SQLClauseParserSimple constructor comment.
     */
    protected SQLClauseParserAbstract(final String input, final Writer output) {
        inputStream = new ReadStream(input);
        resultWriter = output;
    }

    /**
     * Add some SQL expression @aString to the result
     */
    protected void addParseExpression(final String aString) throws IOException {
        resultWriter.write(aString);
    }

    /**
     * Add the parameter identified by aString to the result.
     */
    protected abstract void addParseParamExpression(String aString);

    protected abstract void addParseParamBracket(String aString) throws IOException;

    /**
     * Add sql constant text aString to the parse result
     */
    private void addParseText(final String aString) throws IOException {
        resultWriter.write(aString);
    }

    /**
     * SQL expression at current position.
     * Remember the beginning of the SQL expression in position
     */
    private void foundExpression() {
        if (!expressionMode) {
            position = inputStream.position();
            expressionMode = true;
        }
    }

    /**
     * simple parameter name expected after the parameter marker.
     * Parse and handle the parameter
     */
    protected void foundParamExpression() throws IOException {
        position = inputStream.position();
        boolean cont;
        do {
            cont = !inputStream.atEnd();
            if (cont) {
                final char next = inputStream.next();
                if (isParamExpressionTerminator(next)) {
                    //inputStream.skip(-1);
                    cont = false;
                }
            }
        } while (cont);
        final int end = inputStream.position() + 1;
        if (position < end) {
            addParseParamExpression(inputStream.substring(position, end));
        }
    }

    protected void skip(final int chars) {
        inputStream.skip(chars);
    }

    protected boolean isParamExpressionTerminator(final char c) {
        return !Character.isLetterOrDigit(c);
    }

    /**
     * Parameter marker found at current position.
     * Parse the parameter expression.
     */
    protected void foundParamMarker() throws IOException, ParseException {
        handleExpression(false);
        if (inputStream.atEnd()) {
            signalUnexpectedToken("end of expression after ?");
        }
        position = inputStream.position();
        final char next = inputStream.next();
        if (next == INPUT_PARAM_INDICATOR) {
            foundQuestionMark();
        } else if (next == INPUT_BRACKET_OPEN) {
            foundParamOpenBracket();
        } else if (Character.isWhitespace(next)) {
            signalUnexpectedToken("blank after ?");
        } else {
            foundParamExpression();
        }
    }

    /**
     * An open bracket found at current position as the
     * beginning of a parameter expression.
     * Parse the parameter expression in brackets.
     */
    protected void foundParamOpenBracket() throws IOException, ParseException {
        position = inputStream.position() + 1;
        boolean cont = true;
        do {
            if (inputStream.atEnd())
                signalMissingToken(String.valueOf(INPUT_BRACKET_CLOSE));
            final char next = inputStream.next();
            if (next == INPUT_BRACKET_CLOSE) {
                cont = false;
            }
        } while (cont);
        final int end = inputStream.position();
        if (position < end) {
            addParseParamBracket(inputStream.substring(position, end));
        }
    }


    /**
     * Add a question mark to the SQL expression, that is not a parameter marker
     */
    private void foundQuestionMark() throws IOException {
        addParseExpression(RESULT_PARAM_INDICATOR);
    }

    /**
     * Text delimiter for a text constant found at current position.
     * Scan until the end of the text constant
     */
    private void foundTextDelimiter() throws ParseException, IOException {
        handleExpression(false);
        position = inputStream.position();
        char next = 0;
        while (!(inputStream.atEnd() ||
                (next = inputStream.next()) == INPUT_TEXT_INDICATOR)) ;
        if (next != INPUT_TEXT_INDICATOR) {
            signalMissingToken("text delimiter");
        }
        final int end = inputStream.position() + 1;
        addParseText(inputStream.substring(position, end));
    }

    protected final Writer getResultWriter() {
        return resultWriter;
    }

    /**
     * SQL Expression terminated. Let it be added to the parse result.
     * atEnd is 'true' when this is called after the stream has been completely parsed to add possibly pending expression.
     * atEnd is 'false' when this is called during parsing (the stream position contains another token)
     */
    protected void handleExpression(final boolean atEnd) throws IOException {
        if (expressionMode) {
            final int end = (atEnd) ? inputStream.position() + 1 : inputStream.position();
            if (position <= end) {
                addParseExpression(inputStream.substring(position, end));
            }
        }
        expressionMode = false;
    }

    protected void initForParse() {
        expressionMode = false;
    }

    /**
     * Begin parse and write to resultStream
     */
    public void parse() throws IOException, ParseException {
        initForParse();
        while (!inputStream.atEnd()) {
            final char next = inputStream.next();
            switch (next) {
                case INPUT_TEXT_INDICATOR:
                    foundTextDelimiter();
                    break;
                case INPUT_PARAM_INDICATOR:
                    foundParamMarker();
                    break;
                default:
                    foundExpression();
            }
        }
        handleExpression(true);
    }

    protected void signalMissingToken(final String aSyntaxElement) throws ParseException {
        throw new ParseException("Missing " + aSyntaxElement + " at " +
                inputStream.substring(position, inputStream.size()), position);
    }

    protected void signalUnexpectedToken(final String aString) throws ParseException {
        throw new ParseException("Syntax error: Unexpected token " + aString + " at " +
                inputStream.substring(position, inputStream.size()), position);
    }

}

/**
 * Internal -
 * (might be replaces by a StringCharacterIterator or another class in future releases)
 * <p/>
 * A minimal ReadStream to iterate over a String.
 * This class is used by the <code>SQLClauseParserAbstract</code>
 * and offers a simple protocoll for its purposes.
 *
 * @see SQLClauseParserAbstract
 */
class ReadStream {
    private final String string;
    private int nextPos = -1;
    private static final char EOF = (char) -1;

    /**
     * ReadStream constructor comment.
     */
    ReadStream(final String input) {
        string = input;
    }

    public boolean atEnd() {
        return (nextPos + 1) >= string.length();
    }

    /**
     * read the next char
     * return EOF or the char
     * increase the position
     */
    public char next() {
        if (atEnd()) {
            return EOF;
        }
        return string.charAt(++nextPos);
    }

    public int position() {
        return nextPos;
    }

    public int size() {
        return string.length();
    }

    public void skip(final int count) {
        nextPos += count;
    }

    /**
     * Returns a new string that is a substring of this string. The
     * substring begins at the specified <code>beginIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex   the ending index, exclusive.
     * @return the specified substring.
     * @throws StringIndexOutOfBoundsException
     *          if the
     *          <code>beginIndex</code> or the <code>endIndex</code> is
     *          out of range.
     */
    public String substring(final int beginIndex, final int endIndex) {
        return string.substring(beginIndex, endIndex);
    }

    public String toString() {
        return string;
    }

}
