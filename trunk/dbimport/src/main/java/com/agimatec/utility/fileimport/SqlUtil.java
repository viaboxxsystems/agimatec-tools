package com.agimatec.utility.fileimport;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: Utility class that helps to convert parameters during
 * import processing (e.g. in groovy scripts).<br/>
 * User: roman.stumm <br/>
 * Date: 30.08.2007 <br/>
 * Time: 11:59:08 <br/>
 */
public class SqlUtil implements Serializable {
    private static final String[] SEQ =
            {"nextval(''{0}'')", "{0}.NEXTVAL", "NEXT VALUE FOR {0}"}; // postgres, oracle, hsqldb
    private static final String[] SEQ_FETCH =
            {"SELECT {0}", "SELECT {0} FROM DUAL", "SELECT {0}"}; // postgres, oracle, hsqldb
    private static final int POSTGRES = 0;
    private static final int ORACLE = 1;
    private static final int HSQLDB = 2;
    private final int dbms; // POSTGRES, ORACLE

    /** Ermittele die Datenbank anhand der MetaData der Connection */
    public static SqlUtil forConnection(Connection connection) throws SQLException {
        String dbms = connection.getMetaData().getDatabaseProductName();
        if ("Oracle".equals(dbms)) {
            return SqlUtil.forOracle();
        } else if ("PostgreSQL".equals(dbms)) {
            return SqlUtil.forPostgres();
        } else {
            return SqlUtil.getDefault();
        }
    }

    public boolean isOracle() {
        return dbms == ORACLE;
    }

    public boolean isPostgres() {
        return dbms == POSTGRES;
    }

    public boolean isHSQLDB() {
        return dbms == HSQLDB;
    }

    /** create an instance for postgres syntax (affects sequences) */
    public static SqlUtil forPostgres() {
        return new SqlUtil(POSTGRES);
    }

    /** create an instance for oracle syntax (affects sequences) */
    public static SqlUtil forOracle() {
        return new SqlUtil(ORACLE);
    }

    /** create an instance for HSQLDB syntax (affects sequences) */
    public static SqlUtil forHSQLDB() {
        return new SqlUtil(HSQLDB);
    }

    public static SqlUtil getDefault() {
        return forPostgres();
    }

    protected SqlUtil(int dbms) {
        this.dbms = dbms;
    }

    private Map<String, Object> calls = new HashMap();

    /**
     * define the format for a date field
     *
     * @param fieldname  - symbolic field name
     * @param dateFormat - java.text.SimpleDateFormat pattern
     */
    public void defDate(String fieldname, String dateFormat) {
        calls.put(fieldname, new SimpleDateFormat(dateFormat));
    }

    /**
     * define the format for a number field
     *
     * @param fieldname    - symbolic field name
     * @param numberFormat - java.text.DecimalFormat pattern
     */
    public void defNumber(String fieldname, String numberFormat) {
        calls.put(fieldname, new DecimalFormat(numberFormat));
    }

    /**
     * define the call that a field gets its value from a sequence
     *
     * @param fieldname    - symbolic field name
     * @param sequenceName - name of the sequence to be used
     */
    public void defSequence(String fieldname, String sequenceName) {
        calls.put(fieldname, MessageFormat.format(SEQ[dbms], sequenceName));
    }

    /**
     * convert a value to a sql.Date
     *
     * @param fieldName - symbolic field name (must have a format registered with defDate())
     * @param value     - text value to be parsed to date, can be "" or null
     * @return java.sql.Date or null
     * @throws ParseException
     */
    public java.sql.Date date(String fieldName, String value) throws ParseException {
        if (value == null) return null;
        value = value.trim();
        if (value.length() == 0) return null;
        Object def = calls.get(fieldName);
        return new java.sql.Date(((DateFormat) def).parse(value).getTime());
    }

    public java.sql.Timestamp timestamp(String fieldName, String value)
            throws ParseException {
        if (value == null) return null;
        value = value.trim();
        if (value.length() == 0) return null;
        Object def = calls.get(fieldName);
        return new java.sql.Timestamp(((DateFormat) def).parse(value).getTime());
    }

    public java.sql.Time time(String fieldName, String value) throws ParseException {
        if (value == null) return null;
        value = value.trim();
        if (value.length() == 0) return null;
        Object def = calls.get(fieldName);
        return new java.sql.Time(((DateFormat) def).parse(value).getTime());
    }

    public Number number(String fieldName, String value) throws ParseException {
        if (value == null) return null;
        value = value.trim();
        if (value.length() == 0) return null;
        Object def = calls.get(fieldName);
        return ((NumberFormat) def).parse(value);
    }

    public boolean isTrue(String value) {
        return "1".equals(value) || Boolean.parseBoolean(value);
    }

    /**
     * Oracle DOES NOT SUPPORT Boolean, so this method returns 1 or 0 (or null).
     * In case of Postgres, it returns true or false (or null)
     * @param value null (for null), "1" (for true), "0" (for false), "true" (for true), "false" (for false)
     */
    public Object bool(String value) {
        if (value == null) return null;
        return isOracle() ? Integer.valueOf((isTrue(value) ? 1 : 0)) :
                Boolean.valueOf(isTrue(value));
    }

    public Object bool(boolean value) {
        return isOracle() ? Integer.valueOf((value ? 1 : 0)) :
                Boolean.valueOf(value);
    }

    public String nullable(String value) {
        return value == null || value.length() == 0 ? null : value;
    }

    /** fetch the next value of the sequence, defined under this name */
    public long nextVal(Connection connection, String fieldName) throws SQLException {
        final String sql = MessageFormat.format(SEQ_FETCH[dbms], get(fieldName));
        final Statement stmt = connection.createStatement();
        ResultSet result = null;
        try {
            result = stmt.executeQuery(sql);
            result.next();
            return result.getLong(1);
        } finally {
            if (result != null) result.close();
            stmt.close();
        }

    }

    /**
     * get the call for the given field.
     * can be used to retrieve a sequence.nextvalue expression
     *
     * @param fieldName - symbolic field name
     * @return defined call for the field
     * @throws ParseException
     */
    public Object get(String fieldName) {
        return calls.get(fieldName);
    }

    /**
     * a null-aware trim function
     *
     * @param value - a string or null
     * @return trimmed value or null
     */
    public String trim(String value) {
        return (value == null) ? value : nullable(value.trim());
    }
}
