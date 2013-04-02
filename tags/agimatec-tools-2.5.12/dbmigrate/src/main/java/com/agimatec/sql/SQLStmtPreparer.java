package com.agimatec.sql;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <b>Description:</b>   Replace params in a SQL string so that they can be bound as
 * host-variables when possible.<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
final class SQLStmtPreparer extends SQLStringGenerator {

    private final DateFormat SQLDateFormat;
    private final DateFormat SQLTimestampFormat;
    private final DateFormat SQLTimeFormat;

    {
        SQLDateFormat = new SimpleDateFormat("yyyyMMdd");
        SQLDateFormat.setLenient(
                false); // do not use heuristics for parsing dates not precisely matching the format

        SQLTimestampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SQLTimestampFormat.setLenient(
                false); // do not use heuristics for parsing dates not precisely matching the format

        SQLTimeFormat = new SimpleDateFormat("HHmmss");
        SQLTimeFormat.setLenient(
                false); // do not use heuristics for parsing dates not precisely matching the format
    }

    private List newParams;

    /**
     * @param input  e.g. SELECT * FROM TABLE WHERE OID = ? AND THEDATE = ?
     * @param output a writer for the result
     * @param params e.g. a List containing a String for OID and a Date for THEDATE.
     *               output contains the following after the parse() methods has been executed:
     *               SELECT * FROM TABLE WHERE OID = 'oidvalue' AND THEDATE = to_date('datevalue', 'YYYY-MM-DD')
     */
    SQLStmtPreparer(String input, Writer output, List params) {
        super(input, output, params);
        newParams = new ArrayList(params.size());
    }

    public List getNewParams() {
        return newParams;
    }

    protected void handleParam(final Object aValue) throws IOException {
        if (aValue instanceof java.sql.Date) // special
        {
            addParseExpression(
                    "to_date(?, 'YYYYMMDD')");   // use most compact format (best performance)
            newParams.add(SQLDateFormat.format((java.sql.Date) aValue));
        } else if (aValue instanceof java.sql.Timestamp) {
            addParseExpression("to_date(?, 'YYYYMMDDHH24MISS')");
            newParams.add(SQLTimestampFormat.format((java.sql.Timestamp) aValue));
        } else if (aValue instanceof java.sql.Time) {
            addParseExpression("to_date(?, 'HH24MISS')");
            newParams.add(SQLTimeFormat.format((java.sql.Time) aValue));
        } else if (aValue instanceof Collection) {
            final Iterator iter = ((Collection) aValue).iterator();
            while (iter.hasNext()) {
                handleParam(iter.next());
                if (iter.hasNext()) {
                    addParseExpression(",");
                }
            }
        } else {
            addParseExpression("?");  // leave SQL and param unchanged
            newParams.add(aValue);
        }
    }
}
