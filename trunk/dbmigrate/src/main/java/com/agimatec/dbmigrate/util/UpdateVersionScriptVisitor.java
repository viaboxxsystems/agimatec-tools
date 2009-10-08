package com.agimatec.dbmigrate.util;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.script.ScriptVisitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 17:39:50 <br/>
 * Copyright: Agimatec GmbH
 */
public class UpdateVersionScriptVisitor extends ScriptVisitorDelegate {
    private static final Log log = LogFactory.getLog(UpdateVersionScriptVisitor.class);
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
        int count = 0;
        PreparedStatement s;
        if (!meta.isInsertOnly()) {
            s = jdbcStore.getConnection()
                    .prepareStatement(meta.toSQLUpdate());
            try {
                setParameters(s, dbVersion, meta);
                count = s.executeUpdate();
            } catch (SQLException ex) { // we assume: no DB_VERSION table in database
                log.warn("cannot update " + meta.getQualifiedVersionColumn() + " = " +
                        dbVersion, ex);
                return;
            } finally {
                s.close();
            }
        }
        if (count == 0) {  // no rows affected by update, try insert instead
            s = jdbcStore.getConnection().prepareStatement(meta.toSQLInsert());
            try {
                setParameters(s, dbVersion, meta);
                s.execute();
            } finally {
                s.close();
            }
        }
    }

    private static void setParameters(PreparedStatement s, String dbVersion,
                                      DBVersionMeta meta) throws SQLException {
        s.setString(1, dbVersion);
        if (meta.getColumn_since() != null) {
            s.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }
    }

}