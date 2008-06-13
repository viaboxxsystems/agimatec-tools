package com.agimatec.utility.fileimport;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Description: Responsible to simplify the management of
 * multiple imports that should not run parallel.
 * Relies on database table 'IMPORT_CONTROL', accessed via JDBC.<br/>
 * User: roman.stumm <br/>
 * Date: 30.08.2007 <br/>
 * Time: 13:02:51 <br/>
 * Copyright: Agimatec GmbH
 */
public class ImportController {
    protected static final Logger log = Logger.getLogger(ImportController.class);
    public static final String STATUS_IDLE = "IDLE";  // import not yet started
    public static final String STATUS_RUNNING = "RUNNING"; // import currently running
    public static final String STATUS_DONE =
            "DONE";  // import completly done (with or without errors)
    public static final String STATUS_CANCELLED =
            "CANCELLED"; // import cancelled, finished abnormally

    private Connection connection;
    private SqlUtil sqlUtil;
    private String select;
    private String insert;
    private String update;
    private String end;
    private String delete;

    private void initStatements() {
        select =
                "SELECT import_id, start_time, end_time, status, row_count, error_count FROM Import_Control " +
                        "WHERE import_name = ?";
        insert =
                "INSERT INTO Import_Control (import_id, start_time, end_time, status, import_name) " +
                        "VALUES (" + sqlUtil.get("import_id") + ", ?, ?, ?, ?)";
        update =
                "UPDATE Import_Control SET start_time = ?, end_time = ?, status = ? WHERE import_name = ?";
        end =
                "UPDATE Import_Control SET end_time = ?, status = ?, row_count = ?, error_count = ? WHERE import_name = ?";
        delete = "DELETE FROM Import_Control WHERE Import_Name = ?";
    }

    /** @param util - muss "import_id" als sequenz definiert haben!! */
    public ImportController(Connection connection, SqlUtil util) {
        this.connection = connection;
        this.sqlUtil = util;
        initStatements();
    }

    /** @param sequenceName - name der sequenz, die primary keys generieren kann */
    public ImportController(Connection connection, SqlUtil util, String sequenceName) {
        this.connection = connection;
        this.sqlUtil = util;
        util.defSequence("import_id", sequenceName);
        initStatements();
    }

    public ImportControl findByName(String importName) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(select);
        try {
            selectStmt.setString(1, importName);
            ResultSet result = selectStmt.executeQuery();
            if (result.next()) {
                ImportControl row = new ImportControl();
                row.importId = result.getLong(1);
                row.startTime = result.getTimestamp(2);
                row.endTime = result.getTimestamp(3);
                row.status = result.getString(4);
                row.importName = importName;
                row.rowCount = result.getInt(5);
                if (result.wasNull()) row.rowCount = null;
                row.errorCount = result.getInt(6);
                if (result.wasNull()) row.errorCount = null;
                result.close();
                return row;
            } else return null;
        } finally {
            selectStmt.close();
        }
    }

    /**
     * loescht den import aus der Kontrolltabelle.
     *
     * @return true wenn etwas geloescht wurde, sonst false
     * @throws SQLException
     */
    public boolean delete(String importName) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(delete);
        try {
            stmt.setString(1, importName);
            return stmt.executeUpdate() > 0;
        } finally {
            stmt.close();
        }
    }

    /**
     * legt den import mit dem angeg. Namen in der Datenbank an.
     * Falls der Import noch läuft, wartet das script bis er fertig ist.
     * (Weil es auf einen Datenbank-Lock stößt. Das erfordert allerdings z.Zt.
     * dass Imports immer in einer einzigen Transaktion bearbeitet werden können -
     * bei sehr großen Datenmengen trifft das ggf. nicht zu. In diesen Fällen
     * wäre diese Klasse um weitere Strategien für 'lange' Transaktionen zu erweitern)
     *
     * @throws SQLException
     */
    public void join(String importName) throws SQLException {
        if (log.isInfoEnabled()) log.info("Starting import '" + importName + "'...");
        PreparedStatement updateStmt = connection.prepareStatement(update);
        PreparedStatement insertStmt = null;
        try {
            setSaveParameters(updateStmt, importName);
            if (updateStmt.executeUpdate() == 0) {
                insertStmt = connection.prepareStatement(insert);
                setSaveParameters(insertStmt, importName);
                insertStmt.execute();
            }
        } finally {
            updateStmt.close();
            if (insertStmt != null) insertStmt.close();
        }
    }

    /**
     * markiert den import mit dem angeg. Namen als beendet. Falls er noch
     * nicht in der Datenbank stand, wird er jetzt angelegt.
     *
     * @throws SQLException
     */
    public void end(String importName, String status, Integer rowCount,
                    Integer errorCount) throws SQLException {
        PreparedStatement updateStmt = connection.prepareStatement(end);
        PreparedStatement insertStmt = null;
        try {
            setEndParameters(updateStmt, status, rowCount, errorCount, importName);
            if (updateStmt.executeUpdate() == 0) {
                insertStmt = connection.prepareStatement(insert);
                insertStmt.setTimestamp(1, now());
                insertStmt.setTimestamp(2, now());
                insertStmt.setString(3, status);
                insertStmt.setString(4, importName);
                insertStmt.execute();
                if (rowCount != null || errorCount != null) {
                    updateStmt.executeUpdate();
                }
            }
        } finally {
            updateStmt.close();
            if (insertStmt != null) insertStmt.close();
        }
    }

    /**
     * convenience method that takes infos from the importer.
     *
     * @param importer - a fileimport after import has ended
     * @throws SQLException
     */
    public void end(String importName, Importer importer) throws SQLException {
        String status = importer.isCancelled() ? STATUS_CANCELLED : STATUS_DONE;
        if (log.isInfoEnabled()) log.info(status + ": Import '" + importName +
                "' has finished " + importer.getRowCount() + " rows with " +
                importer.getErrorCount() + " errors.");
        end(importName, status, importer.getRowCount(), importer.getErrorCount());
    }

    private void setEndParameters(PreparedStatement updateStmt, String status,
                                  Integer rowCount, Integer errorCount, String importName)
            throws SQLException {
        updateStmt.setTimestamp(1, now());
        updateStmt.setString(2, status);
        updateStmt.setObject(3, rowCount);
        updateStmt.setObject(4, errorCount);
        updateStmt.setString(5, importName);
    }

    private void setSaveParameters(PreparedStatement stmt, String importName)
            throws SQLException {
        stmt.setTimestamp(1, now());
        stmt.setTimestamp(2, null);
        stmt.setString(3, STATUS_RUNNING);
        stmt.setString(4, importName);
    }

    public SqlUtil getSqlUtil() {
        return sqlUtil;
    }

    private static java.sql.Timestamp now() {
        return new java.sql.Timestamp(System.currentTimeMillis());
    }
}
