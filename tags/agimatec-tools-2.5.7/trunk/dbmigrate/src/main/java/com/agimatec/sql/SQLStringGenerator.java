package com.agimatec.sql;

import com.agimatec.commons.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

/**
 * Generates an SQL String (with parameters) from the given SQL-statement (containing parameter markers)
 * and a given parameter array.
 * Also handles string constants correctly.
 * <p/>
 * Caution: Uses Oracle Date String format!
 * <p/>
 * $Author: stumm $
 */
public class SQLStringGenerator extends SQLClauseParserAbstract {
    protected final List params;
    protected int paramIdx;
    private SQLDateFormats df;

    /**
     * @param input   e.g. SELECT * FROM TABLE WHERE OID = ? AND THEDATE = ?
     * @param output  a writer for the result
     * @param aParams e.g. a List containing a String for OID and a Date for THEDATE.
     *                output contains the following after the parse() methods has been executed:
     *                SELECT * FROM TABLE WHERE OID = 'oidvalue' AND THEDATE = to_date('datevalue', 'YYYY-MM-DD')
     */
    public SQLStringGenerator(final String input, final Writer output, final List aParams) {
        super(input, output);
        this.params = aParams;
        paramIdx = 0;
    }

    /**
     * Add the parameter identified by aString to the result.
     */
    protected void addParseParamExpression(final String aString) {
        throw new RuntimeException("not supported");
    }

    protected void addParseParamBracket(final String aString) {
        throw new RuntimeException("not supported");
    }

    /**
     * Parameter marker found at current position.
     * Parse the parameter expression.
     */
    protected void foundParamMarker() throws IOException {
        handleExpression(false);
        final Object value;
        if (paramIdx < params.size()) {
            value = params.get(paramIdx);
        } else value = null;
        handleParam(value);
        paramIdx++;
    }

    protected void handleParam(final Object aValue) throws IOException {
        addParseExpression(toSQLParamString(aValue));
    }

    protected String toSQLParamString(final Object value) {
        if (value == null) return "NULL";
        final StringBuilder buf = new StringBuilder();
        appendSQLParamString(buf, value);
        return buf.toString();
    }

    public void appendSQLParamString(final StringBuilder buf, final Object value) {
        if (value == null) {
            buf.append("NULL");
            return;
        }

        if (value instanceof java.util.Date) {
            if(df == null) df = new SQLDateFormats();
            if (value instanceof java.sql.Date) // special
            {
                buf.append("to_date('");
                buf.append(df.DateFormYYYYMMDD.format((Date) value));
                buf.append("', 'YYYY-MM-DD')");
            } else if (value instanceof java.sql.Timestamp) {
                buf.append("to_date('");
                buf.append(df.TimestampFormYYYYMMDDHHmmss.format((Date) value));
                buf.append("', 'YYYY-MM-DD HH24:MI:SS')");
            } else if (value instanceof java.sql.Time) {
                buf.append("to_date('");
                buf.append(df.TimeFormHHMMSS.format((Date) value));
                buf.append("', 'HH24:MI:SS')");
            }
        } else if (value instanceof String) {
            StringUtils.appendSQLLiteral((String) value, buf);
        } else if (value instanceof Collection) {
            final Iterator iter = ((Collection) value).iterator();
            while (iter.hasNext()) {
                appendSQLParamString(buf, iter.next());
                if (iter.hasNext()) buf.append(',');
            }
        } else {
            buf.append(value);
        }
    }

    /**
     * convert the sql to be a valid PL/SQL Stored Procudure call.
     * Oracle expects different SQL than the JDBC standard.
     * JDBC Standard: "call <procname>"
     * Oracle: "begin <procname>;end;"
     *
     * @param sql
     * @return the sql decorated to be a valid procedure call.
     */
    public static String asProcedureCall(String sql) {
        return "begin " + sql + ";end;";
    }

}

