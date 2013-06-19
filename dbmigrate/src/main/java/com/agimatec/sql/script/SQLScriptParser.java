package com.agimatec.sql.script;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.util.ClassUtils;
import com.agimatec.commons.util.PropertyReplacer;
import com.agimatec.jdbc.JdbcException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;

/**
 * <p>Title: Agimatec GmbH</p>
 * <p>Description: This class is capable to parse SQL scripts. It is not a fully SQL and PL/SQL parser and
 * somehow heuristic.<br>
 * Able to parse:<br>
 * - SQL-statements (insert, update, select, delete, merge, synonyms, grants, create table, alter table, ...).
 * Statements terminated by ; are correctly handled.
 * Statements terminated by / are correctly handled in most situations.<br>
 * - SQL-comments (single- and multi-line comments)
 * - commit/rollback statements are detected
 * - Triggers<br>
 * - PL/SQL blocks<br>
 * Known limitations/bugs:<br>
 * - parsing PL/SQL package files is currently not supported!<br>
 * - parsing PL/SQL (triggers, blocks) is not always correct, when statement termination is not detected by this parser
 * correctly. (Just try it.)
 * </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Agimatec GmbH </p>
 *
 * @author Roman Stumm
 */
public class SQLScriptParser {
    private static final Logger myClassLogger = LoggerFactory.getLogger(SQLScriptParser.class);
    private Logger myLog = myClassLogger;
    private boolean myFailOnError = false;
    private final String myScriptRoot;
    /**
     * optional - environment map
     */
    private PropertyReplacer myEnvReplacer = null;

    /**
     * SEPARATOR DECLARATION
     */
    private static final String COMMENT_LINE = "--";
    private static final String COMMENT_MULTILINE_BEGIN = "/*";
    private static final String COMMENT_MULTILINE_END = "*/";
    private static final String LITERAL = "'";
    private static final String SEMICOLON = ";";
    private static final String SLASH = "/";
    private static final String[] PROCEDURE_OR_TRIGGER = new String[]{"begin", "declare",
            "cursor"}; // keywords to detect a SQL ended by / only

    private static final String[] SQL_SEPARATORS;

    static {
        String[] seps = new String[]{SEMICOLON, "\r", "\n", LITERAL,
                COMMENT_MULTILINE_BEGIN, COMMENT_LINE, " ", "\t"};
        SQL_SEPARATORS = new String[seps.length + PROCEDURE_OR_TRIGGER.length];
        System.arraycopy(seps, 0, SQL_SEPARATORS, 0, seps.length);
        System.arraycopy(PROCEDURE_OR_TRIGGER, 0, SQL_SEPARATORS, seps.length,
                PROCEDURE_OR_TRIGGER.length);
    }

    private static final String[] PROCEDURE_SEPARATORS = {"\n", "\r"};

    public SQLScriptParser(String aScriptRoot, Logger aLog) {
        myLog = aLog;
        myScriptRoot = aScriptRoot;
    }

    public SQLScriptParser(Logger aLog) {
        myScriptRoot = null;
        myLog = aLog;
    }

    public void useLogger(Logger aLogger) {
        myLog = aLogger;
    }

    /**
     * default = false
     * true: stop executing the SQLScript when the first error occurs and throw an exception.
     * false: log errors to log and continue without throwing an exception.
     *
     * @param aFailOnError - true/false
     */
    public void setFailOnError(boolean aFailOnError) {
        myFailOnError = aFailOnError;
    }

    protected void handleAffectedRow(int affectedRows, String command) {
        if (getLog().isInfoEnabled()) {
            getLog().info(affectedRows + " rows affected");
        }
    }

    protected void handleError(SQLException ex, String command) throws SQLException {
        getLog().error("SQL-EXCEPTION: " + ex.getMessage());
        if (myFailOnError) {
            throw ex;
        }
    }

    protected void handleError(JdbcException ex, String command) throws JdbcException {
        getLog().error("JDBC-EXCEPTION: " + ex.getMessage());
        if (myFailOnError) {
            throw ex;
        }
    }

    protected Logger getLog() {
        return myLog;
    }

    /**
     * set a Map of environment entries that are replaced during parse inside the SQL Statements/comments
     */
    public void setEnvironment(Map aEnv) {
        if (aEnv == null) {
            myEnvReplacer = null;
        } else {
            myEnvReplacer = new PropertyReplacer(aEnv);
        }
    }

    /**
     * @return the root dir for sql scripts or null
     */
    protected String getScriptDir() {
        return myScriptRoot;
    }

    /**
     * Tested with: Oracle10.2
     * fix \r\n --> \n                (always, otherwise the package will be invalid)
     * remove last / but keep last ; (optional, only if a / was found after a ;)
     *
     * @param aStatement - file content as String
     * @return file content as String the can be executed as a single SQL statement via Oracle-JDBC
     */
    protected String fixLF(String aStatement) {
        int t = aStatement.lastIndexOf('/');
        int idx = aStatement.lastIndexOf(';');
        if (idx > 0 && t > idx) {
            aStatement = aStatement.substring(0, idx + 1);
        }
        return aStatement.replace("\r\n", "\n");
    }

    /**
     * parse and visit the statements in the given sql-script file.
     *
     * @throws SQLException - errors during execution of the SQL statements in the script
     * @throws IOException  - error accessing the script file (e.g. FileNotFound)
     */
    public void iterateSQLScript(ScriptVisitor visitor, String scriptName)
            throws SQLException, IOException {
        Object[] readerPath = openReaderPath(scriptName);
        final Reader input = (Reader) readerPath[0];
        final String path = (String) readerPath[1];
        if (getLog().isInfoEnabled()) {
            getLog().info("Parsing " + path + " ... ");
        }
        try {
            iterateSQL(visitor, input);
        } finally {
            input.close();
        }
        if (getLog().isInfoEnabled()) {
            getLog().info("DONE with " + path);
        }
    }

    /**
     * read and visit the statements in the given sql-script file.
     * The file is in a simple format (like generated by eclipselink):
     * each line is a complete statement and no ; delimiters are used to separate the statements.
     *
     * @throws SQLException - errors during execution of the SQL statements in the script
     * @throws IOException  - error accessing the script file (e.g. FileNotFound)
     */
    public void iterateSQLLines(ScriptVisitor visitor, String scriptName) throws SQLException, IOException {
        Object[] readerPath = openReaderPath(scriptName);
        final BufferedReader input = (BufferedReader) readerPath[0];
        final String path = (String) readerPath[1];
        if (getLog().isInfoEnabled()) {
            getLog().info("Executing line by line " + path + " ... ");
        }
        try {
            ParseState parseState = new ParseState(visitor);
            String statement = input.readLine();
            while (statement != null) {
                statement = finish(statement);
                if (statement.startsWith(COMMENT_LINE)) {
                    visitor.visitComment(statement);
                } else {
                    parseState.appendSql(statement);
                    parseState.visitSql();
                }
                statement = input.readLine();
            }
        } finally {
            input.close();
        }
        if (getLog().isInfoEnabled()) {
            getLog().info("DONE with " + path);
        }
    }

    /**
     * execute the content of a file as a single SQL statement.
     * You can use this, when you need not parse the file or when the file cannot be parsed.
     * Example: use this to execute a PL/SQL package, that is stored in a single file (1 file for the spec,
     * 1 file for the body).
     */
    public void execSQLScript(ScriptVisitor visitor, String scriptName)
            throws IOException, SQLException {
        Object[] readerPath = openReaderPath(scriptName);
        final Reader input = (Reader) readerPath[0];
        final String path = (String) readerPath[1];
        if (getLog().isInfoEnabled()) {
            getLog().info("Reading and executing " + path + " ... ");
        }
        String statement = IOUtils.toString(input);
        statement = finish(statement);
        statement = fixLF(statement);
        ParseState parseState = new ParseState(visitor);
        parseState.appendSql(statement);
        parseState.visitSql();
        if (getLog().isInfoEnabled()) {
            getLog().info("DONE with " + path);
        }
    }

    /**
     * when scriptName starts with cp:// read the scriptName as a resource
     * from the classpath, otherwise access the script as a file or cp:// resource by
     * scriptdir + scriptname.
     *
     * @param scriptName
     * @return an array with 2 elements. array[0] = Reader, array[1] = String (Path)
     * @throws IOException - file not found
     */
    protected Object[] openReaderPath(String scriptName) throws IOException {
        final Reader input;
        String path;
        if (scriptName.startsWith(ConfigManager.C_ProtocolClassPath)) {
            URL ress = ClassUtils.getClassLoader().getResource(scriptName.substring(5));
            path = ress.toExternalForm();
            input = new BufferedReader(new InputStreamReader(ress.openStream()));
        } else {
            path = (getScriptDir() != null) ? getScriptDir() + scriptName : scriptName;
            URL ress = ConfigManager.toURL(path);
            if (ress == null) {
                throw new FileNotFoundException(path);
            }
            input = new BufferedReader(new InputStreamReader(ress.openStream()));
        }
        return new Object[]{input, path};
    }

    /**
     * parse an visit the statements in the given sql-script file.
     *
     * @param url - a complete URL (absolute URL) where the script is
     * @throws SQLException - errors during execution of the SQL statements in the script
     * @throws IOException  - error accessing the script file (e.g. FileNotFound)
     */
    public void iterateSQLScript(ScriptVisitor visitor, URL url)
            throws SQLException, IOException {
        if (getLog().isInfoEnabled()) {
            getLog().info("Parsing " + url + " ... ");
        }
        Reader input = getURLReader(url);
        try {
            iterateSQL(visitor, input);
        } finally {
            input.close();
        }
        if (getLog().isInfoEnabled()) {
            getLog().info("DONE with " + url);
        }
    }

    public static Reader getURLReader(URL url) throws IOException {
        final InputStream fs = url.openStream();
        return new InputStreamReader(fs);
    }

    /**
     * parse and visit the statements in the given sql string.
     *
     * @param aSqls   - a SQLScript as a String
     * @param visitor - the visitor to do something with the parsed statements
     * @throws java.sql.SQLException
     */
    public void iterateSQL(ScriptVisitor visitor, String aSqls)
            throws SQLException, IOException {
        Reader input = new StringReader(aSqls);
        iterateSQL(visitor, input);
    }

    /* * * * * * * * * * * * *
*  BEGIN PARSER STUFF   *
* * * * * * * * * * * * */

    /**
     * parse and visit the statements in the given sql string.
     *
     * @param input   - a Reader on sql statements
     * @param visitor - the visitor to do something with the parsed statements
     * @throws java.sql.SQLException
     */
    public void iterateSQL(ScriptVisitor visitor, Reader input)
            throws SQLException, IOException {
        parseSQL(new ParseState(visitor), input);
    }

    /* other parse loop */
    private void parseSQL(final ParseState state, final Reader input)
            throws IOException, SQLException {
        final WordTokenizer tokens =
                new WordTokenizer(input, SQL_SEPARATORS, true, false);
        String token = tokens.nextToken();
        int procMode =
                1; // 1 = sep. before (prepare pro), 2 = "BEGIN" detected afterwards, 0 = other (no proc)
        while (token != null) {
            if (LITERAL.equals(token)) {
                procMode = 0;
                state.appendSql(token);
                tokens.addChar(parseLiteral(state, tokens));
            } else if (COMMENT_LINE.equals(token)) {
                parseCommentLine(state, tokens);
            } else if (COMMENT_MULTILINE_BEGIN.equals(token)) {
                parseCommentMultiLine(state, tokens);
            } else if ("\n".equals(token) || "\r".equals(token)) {
                state.needsBlank = true;
                if (state.isSlashLine()) { // a line with only "/" terminates always!
                    state.visitSql();
                }
                state.newLine();
                if (procMode == 2) {
                    if ("\n".equals(token)) {
                        state.appendPlain(token);
                    }
                    detectProcedure(state, tokens);
                }
                procMode = 1;
            } else if (SEMICOLON.equals(token)) {
                if (procMode == 2) {
                    detectProcedure(state, tokens);
                }
                state.visitSql();
                procMode = 1;
            } else if (ArrayUtils
                    .indexOf(PROCEDURE_OR_TRIGGER, token.toLowerCase(), 0) >= 0) {
                state.appendCurrentSql(token);
                if (procMode == 1) {
                    procMode = 2;
                } else {
                    procMode = 0;
                }
            } else if (" ".equals(token) || "\t".equals(token)) {
                state.appendPlain(" ");
                if (procMode == 2) {
                    detectProcedure(state, tokens);
                }
                procMode = 1;
            } else {
                procMode = 0;
                if (token.trim().length() > 0) { // avoid empty lines
                    state.appendCurrentSql(token);
                }
            }
            token = tokens.nextToken();
        }
        if (!state.isEmpty()) {
            state.visitSql();
        }
    }

    private void detectProcedure(ParseState state, WordTokenizer tokens)
            throws SQLException, IOException {
        parseProcedure(state, tokens);
        tokens.setSeparators(SQL_SEPARATORS);
    }

    /* parse trigger, procedure, function (all that end with / and may contains comments etc) */
    private void parseProcedure(ParseState state, WordTokenizer tokens)
            throws IOException, SQLException {
        tokens.setSeparators(PROCEDURE_SEPARATORS);
        String token = tokens.nextToken();
        while (token != null) {  /* include comments. do not extract them */
            if ("\r".equals(token)) {
                if (state.procNewLine()) {
                    return;
                }
            } else if ("\n".equals(token)) {
                state.appendSql("\n");
                if (state.procNewLine()) {
                    return;
                }
            } else {
                state.appendCurrentSql(token);
            }
            token = tokens.nextToken();
        }
    }

    private void parseCommentMultiLine(ParseState state, WordTokenizer parent)
            throws IOException, SQLException {
        WordTokenizer tokens =
                new WordTokenizer(parent, new String[]{COMMENT_MULTILINE_END}, false, true);
        state.visitor.visitComment(COMMENT_MULTILINE_BEGIN + finish(tokens.nextToken()) +
                COMMENT_MULTILINE_END);
        state.needsBlank = true;
        tokens.setReturnTokens(true);
        tokens.nextToken();
        parent.continueFrom(tokens);
    }

    private void parseCommentLine(ParseState state, WordTokenizer parent)
            throws IOException, SQLException {
        WordTokenizer tokens =
                new WordTokenizer(parent, new String[]{"\n", "\r"}, true, true);
        state.visitor.visitComment(COMMENT_LINE + finish(tokens.nextToken()));
        state.needsBlank = true;
        parent.continueFrom(tokens);
    }

    /**
     * @return the next char (already read from input) that does belong to outer tokenizer
     * @throws IOException
     */
    private int parseLiteral(ParseState state, WordTokenizer parent) throws IOException {
        WordTokenizer tokens =
                new WordTokenizer(parent, new String[]{LITERAL}, true, true);
        try {
            String token = tokens.nextToken();
            while (token != null) {
                if (LITERAL.equals(token)) {
                    int next = tokens.nextChar();
                    state.appendSql(LITERAL);
                    if (LITERAL.charAt(0) != (char) next) { // end of Literal
                        return next;
                    }
                    state.appendSql(LITERAL);
                } else {
                    state.appendSql(token);
                }
                token = tokens.nextToken();
            }
            throw new IllegalArgumentException("Literal not closed: " + state.sql());
        } finally {
            parent.continueFrom(tokens);
        }
    }

    /**
     * @return sql with properties replaced if neccessary
     */
    private String finish(String sql) {
        if (myEnvReplacer == null) {
            return sql;
        } else {
            return myEnvReplacer.replaceProperties(sql);
        }
    }

    /**
     * inner class to hold the state during parsing.
     */
    class ParseState {
        final ScriptVisitor visitor;
        StringBuilder sqlBuf;
        private StringBuilder currentLine;
        boolean needsBlank;

        ParseState(ScriptVisitor aVisitor) {
            visitor = aVisitor;
            newBuf();
            newLine();
        }

        void newBuf() {
            sqlBuf = new StringBuilder();
            needsBlank = false;
        }

        void newLine() {
            currentLine = new StringBuilder();
        }

        void appendSql(String token) {
            if (needsBlank && sqlBuf.length() > 0 &&
                    sqlBuf.charAt(sqlBuf.length() - 1) != ' ' && token.charAt(0) != ' ') {
                sqlBuf.append(' ');
            }
            needsBlank = false;
            sqlBuf.append(token);
        }

        void appendPlain(String token) {
            currentLine.append(token);
            needsBlank = false;
            sqlBuf.append(token);
        }

        void appendCurrentSql(String token) {
            currentLine.append(token);
            if (!isSlashLine()) {
                appendSql(token);
            }
        }

        boolean isEmpty() {
            return sqlBuf.length() == 0;
        }

        String sql() {
            return sqlBuf.toString();
        }

        boolean isSlashLine() {
            return currentLine.toString().trim().equals(SLASH);
        }

        boolean procNewLine() throws SQLException {
            boolean result = false;
            if (isSlashLine()) { // a line with only "/" terminates always!
                final String sql = sql();
                try {
                    execSQL(sql);
                } catch (SQLException ex) {
                    handleError(ex, sql);
                } catch (JdbcException ex) {
                    handleError(ex, sql);
                }
                newBuf();
                result = true;
            }
            newLine();
            return result;
        }

        /* decide if statement, commit or rollback */
        void visitSql() throws SQLException {
            String sql = sql().trim();
            try {
                if ("COMMIT".equalsIgnoreCase(sql)) {
                    visitor.doCommit();
                } else if ("ROLLBACK".equalsIgnoreCase(sql)) {
                    visitor.doRollback();
                } else {
                    execSQL(sql);
                }
            } catch (SQLException ex) {
                handleError(ex, sql);
            } catch (JdbcException ex) {
                handleError(ex, sql);
            }
            newBuf();
        }

        private void execSQL(String sql) throws SQLException, JdbcException {
            if (sql.length() == 0) {
                return;
            }
            sql = finish(sql);
            int affected = visitor.visitStatement(sql);
            if (affected > 0) {
                handleAffectedRow(affected, sql);
            }
        }
    }
}
