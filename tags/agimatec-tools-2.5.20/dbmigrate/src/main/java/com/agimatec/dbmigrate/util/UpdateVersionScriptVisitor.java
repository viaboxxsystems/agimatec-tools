package com.agimatec.dbmigrate.util;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.script.ScriptVisitor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 17:39:50 <br/>
 */
public class UpdateVersionScriptVisitor extends ScriptVisitorDelegate {
    private static final Logger log = LoggerFactory.getLogger(UpdateVersionScriptVisitor.class);
    private final JdbcDatabase jdbcStore;
    private final DBVersionMeta meta;

    /**
     * @param meta - null or an object that tells the names or DB_Version table and its columns
     *             used for sql generation.
     */
    public UpdateVersionScriptVisitor(JdbcDatabase aStore, ScriptVisitor aVisitor,
                                      DBVersionMeta meta) {
        super(aVisitor);
        jdbcStore = aStore;
        this.meta = meta == null ? new DBVersionMeta() : meta;
    }

    public void visitComment(String theComment) throws SQLException {
        super.visitComment(theComment);
        int idx = theComment.indexOf("@version");
        if (idx < 0) idx = theComment.indexOf("#version");
        if (idx >= 0) {
            StringTokenizer tokens = new StringTokenizer(
                theComment.substring(idx + "#version".length()), "()");
            String dbVersion = null;
            if (tokens.hasMoreTokens()) dbVersion = tokens.nextToken();
            if (dbVersion == null) {
                log.warn("cannot find a version in " + theComment);
            } else {
                updateVersionInDatabase(jdbcStore, dbVersion, meta);
            }
        }
    }

    public static void updateVersionInDatabase(JdbcDatabase jdbcStore, String dbVersion,
                                               DBVersionMeta meta) throws SQLException {
        log.info("*** update version to: " + dbVersion);
        if (jdbcStore.getConnection() == null) throw new SQLException("not connected to database");
        int count = 0;
        PreparedStatement s;
        boolean canAccessDbVersionTable = true;
        if (!meta.isInsertOnly()) {
            s = jdbcStore.getConnection()
                .prepareStatement(meta.toSQLUpdate());
            try {
                setParameters(s, dbVersion, meta);
                count = s.executeUpdate();
            } catch (SQLException ex) { // we assume: no DB_VERSION table in database
                log.warn("cannot update " + meta.getQualifiedVersionColumn() + " = " +
                    dbVersion + " because " + ex.getMessage());
                canAccessDbVersionTable = createTable(jdbcStore, meta);
                if (!canAccessDbVersionTable) {
                    log.info("autoCreate=false, create table " + meta.getTableName() +
                        " to persist the database version: " +
                        dbVersion);
                }
            } finally {
                s.close();
            }
        }
        if (count == 0 && canAccessDbVersionTable) {  // no rows affected by update, try insert instead
            insertVersion(jdbcStore, dbVersion, meta);
        }
    }

    /**
     * @since 2.5.19
     * @param jdbcStore
     * @param dbVersion
     * @param meta
     * @return
     * @throws SQLException
     */
    public static int insertVersion(JdbcDatabase jdbcStore, String dbVersion, DBVersionMeta meta) throws SQLException {
        PreparedStatement s;
        s = jdbcStore.getConnection().prepareStatement(meta.toSQLInsert());
        try {
            setParameters(s, dbVersion, meta);
            return s.executeUpdate();
        } finally {
            s.close();
        }
    }

    /**
     * @since 2.5.19
     * @param jdbcStore
     * @param dbVersion
     * @param meta
     * @return
     * @throws SQLException
     */
    public static int deleteVersion(JdbcDatabase jdbcStore, String dbVersion, DBVersionMeta meta) throws SQLException {
        PreparedStatement s;
        s = jdbcStore.getConnection().prepareStatement(meta.toSQLDelete());
        try {
            s.setString(1, dbVersion);
            return s.executeUpdate();
        } finally {
            s.close();
        }
    }

    private static boolean createTable(JdbcDatabase jdbcStore, DBVersionMeta meta) throws SQLException {
        if (!meta.isAutoCreate()) {
            return false; // do not autoCreate
        }
        Statement stmt = jdbcStore.getConnection().createStatement();
        try {
            stmt.execute(meta.toSQLCreateTable());
            log.info("autoCreate=true, created table " + meta.getTableName());
        } finally {
            stmt.close();
        }
        return true;
    }

    private static void setParameters(PreparedStatement s, String dbVersion,
                                      DBVersionMeta meta) throws SQLException {
        s.setString(1, dbVersion);
        if (StringUtils.isNotEmpty(meta.getColumn_since())) {
            s.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }
    }

}