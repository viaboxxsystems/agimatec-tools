package com.agimatec.dbmigrate.util;

import com.agimatec.dbmigrate.HaltedException;
import com.agimatec.jdbc.JdbcDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Description: Implements the behavior to prevent dbmigrate to execute more than once at the same time
 * on the same database<br>
 * It INSERTs a "busy" version into the database and DELETEs it afterwards.
 * <p>
 * Date: 14.10.14<br>
 * </p>
 *
 * @since 2.5.19
 */
public class BusyLocker implements DatabaseLocker {
    protected final String BUSY_VERSION = "busy";
    protected static final Logger log = LoggerFactory.getLogger(BusyLocker.class);
    private final DBVersionMeta lockMeta;

    /**
     * number of attempts. -1 = unlimited.
     */
    private int maxAttempts = -1;

    /**
     * number of millis to wait between attempts. default = 10000 (10 seconds)
     */
    private int delayBetweenAttempts = 10 * 1000;

    protected boolean ownLock = false;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getDelayBetweenAttempts() {
        return delayBetweenAttempts;
    }

    public void setDelayBetweenAttempts(int delayBetweenAttempts) {
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public BusyLocker(DBVersionMeta dbVersionMeta) {
        this.lockMeta = createLockMeta(dbVersionMeta);
    }

    public boolean isEnabled() {
        return (lockMeta.getLockBusy() != null && lockMeta.getLockBusy() != DBVersionMeta.LockBusy.No);
    }

    public void lock(JdbcDatabase database) {
        if (lockMeta.getLockBusy() == DBVersionMeta.LockBusy.No) return;
        DBVersionMeta lockMeta = createLockMeta(this.lockMeta);
        try {
            tryLock(database);
        } catch (SQLException ex) {
            if (lockMeta.getLockBusy() == DBVersionMeta.LockBusy.Fail) {
                fail(ex);
            } else if (lockMeta.getLockBusy() == DBVersionMeta.LockBusy.Wait) {
                waitAndRetry(database, 1, ex);
            }
        }
    }

    public DBVersionMeta getLockMeta() {
        return lockMeta;
    }

    protected DBVersionMeta createLockMeta(DBVersionMeta dbVersionMeta) {
        DBVersionMeta lockMeta = dbVersionMeta.copy();
        lockMeta.setInsertOnly(false);
        lockMeta.setTableName(lockMeta.getLockTableName());
        return lockMeta;
    }

    private void tryLock(JdbcDatabase database) throws SQLException {
        Statement stmt = database.getConnection().createStatement();
        SQLCursor cursor = null;
        try {
            cursor = new SQLCursor(stmt, stmt.executeQuery(lockMeta.toSQLSelectVersion()));
            cursor.next();
            cursor.close();
        } catch (SQLException ex) { // assume that lock-table does not yet exist, auto-create
            if(cursor != null) cursor.close();
            log.warn("Cannot access " + lockMeta.getTableName() + ": " + ex.getMessage());
            try {
                UpdateVersionScriptVisitor.createTable(database, lockMeta);
            } catch(SQLException ex2) {
                log.warn("Read exception was: ", ex);
                log.warn("Cannot create " + lockMeta.getTableName() + " write exception was: ", ex2);
                throw ex2;
            }
        } finally {
            stmt.close();
        }

        int count = UpdateVersionScriptVisitor.insertVersion(database, BUSY_VERSION, lockMeta);
        if (count != 1) {
            log.warn(
                lockMeta.toSQLInsert() + " for busy-lock '" + BUSY_VERSION + "' affected " + count +
                    " rows!");
        } else {
            log.info("Required busy-lock '" + BUSY_VERSION + "' on table " + lockMeta.getTableName());
            setOwnLock(true);
        }
    }

    public boolean isOwnLock() {
        return ownLock;
    }

    public void setOwnLock(boolean ownLock) {
        this.ownLock = ownLock;
    }

    private void fail(SQLException ex) {
        throw new HaltedException(
            "Could not require busy-lock '" + BUSY_VERSION + "' from table '" + lockMeta.getLockTableName() +
                "'. " +
                "Perhaps another instance of dbmigrate is currently running on the database or " +
                "the lock was not correctly removed from a previous execution of dbmigrate. " +
                "\nTo remove the lock, execute: " +
                "DELETE FROM " + lockMeta.getTableName() + " WHERE " + lockMeta.getColumn_version() +
                " = '" + BUSY_VERSION + "';", ex);
    }

    private void waitAndRetry(JdbcDatabase database, int attempt, SQLException ex) {
        while (maxAttempts < 0 || attempt < maxAttempts) {
            log.warn("Attempt " + attempt + " to require busy-lock failed. Waiting for " + delayBetweenAttempts +
                " millis to retry...", ex);
            if (delayBetweenAttempts > 0) {
                try {
                    Thread.sleep(delayBetweenAttempts);
                } catch (InterruptedException e) {
                    log.warn("Interrupted while waiting for retry", e);
                }
            }
            try {
                tryLock(database);
                return;
            } catch (SQLException e) {
                ex = e;
                attempt++;
            }
        }

        fail(ex);
    }

    public void unlock(JdbcDatabase database) {
        if (lockMeta.getLockBusy() == DBVersionMeta.LockBusy.No) return;

        if (!isOwnLock()) {
            log.info("Not deleting lock '" + BUSY_VERSION + "' on table " + lockMeta.getTableName() +
                " because this instance does not own it.");
        } else {
            try {
                int count = UpdateVersionScriptVisitor.deleteVersion(database, BUSY_VERSION, lockMeta);
                if (count != 1) {
                    log.warn(
                        lockMeta.toSQLDelete() + " for busy-lock '" + BUSY_VERSION + "' affected " + count +
                            " rows!");
                } else {
                    log.info(
                        "Deleted busy-lock '" + BUSY_VERSION + "' on table " + lockMeta.getTableName());
                    setOwnLock(false);
                }
            } catch (SQLException e) {
                log.error("Failed to delete busy-lock", e);
            }
        }
    }

}
