package com.agimatec.utility.fileimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    protected Connection connection;
    protected SqlUtil sqlUtil;
    protected String selectAll, selectByName, selectById;
    protected String insert;
    protected String lockByName;
    protected String update;
    protected String deleteByName, deleteById;

    private void initStatements() {
        lockByName = "UPDATE Import_Control SET STATUS=STATUS where import_name=?";

        selectAll =
              "SELECT import_id,start_time,end_time,status,row_count,error_count,description,file_name,import_name " +
                    "FROM Import_Control ORDER BY import_id";
        selectByName =
              "SELECT import_id,start_time,end_time,status,row_count,error_count,description,file_name,import_name " +
                    "FROM Import_Control WHERE import_name = ? ORDER BY import_id";
        selectById =
              "SELECT import_id,start_time,end_time,status,row_count,error_count,description,file_name,import_name " +
                    "FROM Import_Control WHERE import_id = ?";
        insert =
              "INSERT INTO Import_Control (start_time, end_time, status, import_name, description, file_name, import_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        update =
              "UPDATE Import_Control SET end_time = ?, status = ?, row_count = ?, error_count = ?, error_message = ? WHERE import_id = ?";
        deleteByName = "DELETE FROM Import_Control WHERE Import_Name = ?";
        deleteById = "DELETE FROM Import_Control WHERE Import_Id = ?";
    }

    /** @param util - a sequence under symbolic name "import_id" must be defined!! */
    public ImportController(Connection connection, SqlUtil util) {
        this.connection = connection;
        this.sqlUtil = util;
        initStatements();
    }

    /** @param sequenceName - name of sequence, to generate primary keys */
    public ImportController(Connection connection, SqlUtil util, String sequenceName) {
        this.connection = connection;
        this.sqlUtil = util;
        util.defSequence("import_id", sequenceName);
        initStatements();
    }

    public List<ImportControl> findAll() throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(selectAll);
        try {
            ResultSet result = selectStmt.executeQuery();
            List<ImportControl> rows = new ArrayList();
            while (result.next()) {
                rows.add(createInstance(result));
            }
            result.close();
            return rows;
        } finally {
            selectStmt.close();
        }
    }

    public List<ImportControl> findByName(String importName) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(selectByName);
        try {
            selectStmt.setString(1, importName);
            ResultSet result = selectStmt.executeQuery();
            List<ImportControl> rows = new ArrayList();
            while (result.next()) {
                rows.add(createInstance(result));
            }
            result.close();
            return rows;
        } finally {
            selectStmt.close();
        }
    }

    public ImportControl findById(long importId) throws SQLException {
        PreparedStatement selectStmt = connection.prepareStatement(selectById);
        try {
            selectStmt.setLong(1, importId);
            ResultSet result = selectStmt.executeQuery();
            ImportControl row = null;
            if (result.next()) {
                row = createInstance(result);
            }
            result.close();
            return row;
        } finally {
            selectStmt.close();
        }
    }

    private ImportControl createInstance(ResultSet result) throws SQLException {
        ImportControl row = new ImportControl();
        row.importId = result.getLong(1);
        row.startTime = result.getTimestamp(2);
        row.endTime = result.getTimestamp(3);
        String str = result.getString(4);
        if (str != null) {
            row.status = ImportState.valueOf(str);
        }
        row.rowCount = result.getInt(5);
        if (result.wasNull()) row.rowCount = null;
        row.errorCount = result.getInt(6);
        if (result.wasNull()) row.errorCount = null;
        row.description = result.getString(7);
        row.fileName = result.getString(8);
        row.importName = result.getString(9);
        return row;
    }

    /**
     * delete the imports with the given name from the control table
     *
     * @return true wenn etwas geloescht wurde, sonst false
     * @throws SQLException
     */
    public boolean delete(String importName) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(deleteByName);
        try {
            stmt.setString(1, importName);
            return stmt.executeUpdate() > 0;
        } finally {
            stmt.close();
        }
    }

    /**
     * delete the import with the given importid (primary key)
     *
     * @return true when something has been deleted, false otherwise (not found)
     * @throws SQLException
     */
    public boolean delete(long importId) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(deleteById);
        try {
            stmt.setLong(1, importId);
            return stmt.executeUpdate() > 0;
        } finally {
            stmt.close();
        }
    }

    /**
     * aquire a lock for imports of the given name.
     * block until lock aquired by database.
     * @param importName
     * @return
     * @throws SQLException
     */
    public boolean lock(String importName) throws SQLException {
        if (log.isInfoEnabled())
            log.info("Aquire lock for import '" + importName + "'...");
        PreparedStatement stmt = connection.prepareStatement(lockByName);
        try {
            stmt.setString(1, importName);
            return stmt.executeUpdate() > 0;
        } finally {
            stmt.close();
        }
    }

    /**
     * create import in database for the type and call start() after
     * a lock() has been aquired.
     * update the import_control table for this kind of import
     * to wait until running imports have finished.
     * (wait with database lock. this requires
     * that imports use a single transaction for accessing the import_control table.
     * in case of importing large datasets, this might be a problem.
     * maybe it is neccessary to extend this class with strategies for long-transactions-support,
     * or you need to use two connections:
     * one for accessing import_control
     * the other to import the data with intermediate commits.
     * <p/>
     * Remember: changes written with insert() or update() are only visible after
     * the running transaction has been committed.
     * So you have to decide
     * a) use autocommit / own short transactions to monitor running imports
     * or
     * b) use single transaction to access importcontrol, so lock() works, but
     * all changes are only visible after import is finished!)
     *
     * @return the new import
     * @throws SQLException
     */
    public ImportControl join(String importName) throws SQLException {
        ImportControl imp = new ImportControl();
        imp.importName = importName;
        join(imp);
        return imp;
    }

    public long join(ImportControl imp) throws SQLException {
        lock(imp.importName);
        return start(imp);
    }

    /**
     * calls start() if not already done so!
     * update status, rowCount, errorCount during running import.
     *
     * @throws SQLException
     */
    public void update(ImportControl imp) throws SQLException {
        PreparedStatement updateStmt = connection.prepareStatement(update);
        try {
            setUpdateParameters(updateStmt, imp);
            if (updateStmt.executeUpdate() == 0) {
                start(imp);
                setUpdateParameters(updateStmt, imp);
                if (imp.rowCount != null || imp.errorCount != null) {
                    updateStmt.executeUpdate();
                }
            }
        } finally {
            updateStmt.close();
        }
    }

    /**
     * update rowCount, errorCount during running import.
     *
     * @throws SQLException
     */
    public void update(ImportControl imp, Importer importer) throws SQLException {
        imp.errorMessage = importer.getLastErrorMessage();
        imp.rowCount = importer.getRowCount();
        imp.errorCount = importer.getErrorCount();
        update(imp);
    }

    /**
     * insert the importcontrol row into the database.
     * set startTime=now() and status=RUNNING with still null.
     * call this only once per instance!
     * @param imp
     * @return
     * @throws SQLException
     */
    public long start(ImportControl imp) throws SQLException {
        if (imp.importId == 0) {
            imp.importId = sqlUtil.nextVal(connection, "import_id");
        }
        if (log.isInfoEnabled()) log.info(
              "Starting import '" + imp.importName + "' (" + imp.importId + ")...");
        PreparedStatement insertStmt = null;
        try {
            insertStmt = connection.prepareStatement(insert);
            if (imp.startTime == null) {
                imp.startTime = now();
            }
            imp.endTime = null;
            if (imp.status == null) {
                imp.status = ImportState.RUNNING;
            }
            setParameters(insertStmt, imp);
            insertStmt.execute();
        } finally {
            if (insertStmt != null) insertStmt.close();
        }
        return imp.getImportId();
    }

    /**
     * markiert den import mit dem angeg. Namen als beendet. Falls er noch
     * nicht in der Datenbank stand, wird er jetzt angelegt.
     *
     * @throws SQLException
     */
    public void end(ImportControl imp) throws SQLException {
        PreparedStatement updateStmt = connection.prepareStatement(update);
        try {
            if (imp.endTime == null) {
                imp.endTime = now();
            }
            setUpdateParameters(updateStmt, imp);
            if (updateStmt.executeUpdate() == 0) {
                start(imp);
                setUpdateParameters(updateStmt, imp);
                if (imp.rowCount != null || imp.errorCount != null) {
                    updateStmt.executeUpdate();
                }
            }
        } finally {
            updateStmt.close();
        }
    }

    /**
     * convenience method that takes infos from the importer.
     *
     * @param importer - a fileimport after import has ended
     * @throws SQLException
     */
    public void end(long importId, Importer importer) throws SQLException {
        ImportControl imp = findById(importId);
        end(imp, importer);
    }

    public void end(ImportControl imp, Importer importer) throws SQLException {
        ImportState status =
              importer.isCancelled() ? ImportState.CANCELLED : ImportState.DONE;
        if (log.isInfoEnabled()) log.info(status + ": Import (" + imp.getImportId() +
              ") '" + imp.getImportName() + "' has finished " + importer.getRowCount() +
              " rows with " + importer.getErrorCount() + " errors.");
        imp.rowCount = importer.getRowCount();
        imp.errorCount = importer.getErrorCount();
        imp.status = status;
        end(imp);
    }

    private void setUpdateParameters(PreparedStatement updateStmt, ImportControl imp)
          throws SQLException {
        updateStmt.setTimestamp(1, imp.endTime);
        updateStmt.setString(2, imp.status.name());
        updateStmt.setObject(3, imp.rowCount);
        updateStmt.setObject(4, imp.errorCount);
        String errmsg = imp.errorMessage;
        if (errmsg != null && errmsg.length() > 2000) {
            // cut message because database limit is 2000
            errmsg = errmsg.substring(0, 2000);
        }
        updateStmt.setString(5, errmsg);
        updateStmt.setLong(6, imp.importId);
    }

    //  (start_time, end_time, status, import_name, description, file_name, import_id)
    private void setParameters(PreparedStatement stmt, ImportControl imp)
          throws SQLException {
        stmt.setTimestamp(1, imp.startTime);
        stmt.setTimestamp(2, imp.endTime);
        if (imp.status != null) {
            stmt.setString(3, imp.status.name());
        } else {
            stmt.setString(3, null);
        }
        stmt.setString(4, imp.importName);
        stmt.setString(5, imp.description);
        stmt.setString(6, imp.fileName);
        stmt.setLong(7, imp.importId);
    }

    public SqlUtil getSqlUtil() {
        return sqlUtil;
    }

    private static java.sql.Timestamp now() {
        return new java.sql.Timestamp(System.currentTimeMillis());
    }
}
