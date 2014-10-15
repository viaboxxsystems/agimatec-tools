package com.agimatec.dbmigrate.util;

import com.agimatec.dbmigrate.HaltedException;
import com.agimatec.jdbc.JdbcDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
public class BusyLocker {
    protected final String BUSY_VERSION = "busy";
    protected static final Logger log = LoggerFactory.getLogger(BusyLocker.class);

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

    public boolean isEnabled(DBVersionMeta dbVersionMeta) {
        return (dbVersionMeta.getLockBusy() != null && dbVersionMeta.getLockBusy() != DBVersionMeta.LockBusy.No);
    }

    public void lockBusy(DBVersionMeta dbVersionMeta, JdbcDatabase database) {
        if (dbVersionMeta.getLockBusy() == DBVersionMeta.LockBusy.No) return;

        if (dbVersionMeta.isInsertOnly()) {
           throw new UnsupportedOperationException("not yet implemented: insertOnly + lock-busy");
            /*
                // update db-version all rows, no change => lock
                lockAll(dbVersionMeta, database); // => does not work when auto-commit enabled
                // count busy-locks => 0
                int count = countBusy(dbVersionMeta, database);
                if (count == 0) {
                    // insert lock: tryLock
                    tryLock(dbVersionMeta, database);
                    // => what now?
                } else {
                    if (dbVersionMeta.getLockBusy() == DBVersionMeta.LockBusy.Fail) {
                        fail(dbVersionMeta, null);
                    } else if (dbVersionMeta.getLockBusy() == DBVersionMeta.LockBusy.Wait) {
                        waitAndRetry(dbVersionMeta, database, 1, null); // => what now?
                    }
                }
             */
        } else {
            try {
                tryLock(dbVersionMeta, database);
            } catch (SQLException ex) {
                if (dbVersionMeta.getLockBusy() == DBVersionMeta.LockBusy.Fail) {
                    fail(dbVersionMeta, ex);
                } else if (dbVersionMeta.getLockBusy() == DBVersionMeta.LockBusy.Wait) {
                    waitAndRetry(dbVersionMeta, database, 1, ex);
                }
            }
        }
    }

    private void lockAll(DBVersionMeta dbVersionMeta, JdbcDatabase database) throws SQLException {
        PreparedStatement stmt = database.getConnection().prepareStatement(dbVersionMeta.toSQLLockAll());
        stmt.execute();
        stmt.close();
    }

    private int countBusy(DBVersionMeta dbVersionMeta, JdbcDatabase database) throws SQLException {
        PreparedStatement stmt;
        stmt = database.getConnection().prepareStatement(dbVersionMeta.toSQLCountVersion());
        stmt.setString(1, BUSY_VERSION);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        resultSet.close();
        stmt.close();
        return count;
    }

    private void tryLock(DBVersionMeta dbVersionMeta, JdbcDatabase database) throws SQLException {
        int count = UpdateVersionScriptVisitor.insertVersion(database, BUSY_VERSION, dbVersionMeta);
        if (count != 1) {
            log.warn(
                dbVersionMeta.toSQLInsert() + " for busy-lock '" + BUSY_VERSION + "' affected " + count +
                    " rows!");
        } else {
            log.info("Required busy-lock '" + BUSY_VERSION + "' on table " + dbVersionMeta.getTableName());
            setOwnLock(true);
        }
    }

    public boolean isOwnLock() {
        return ownLock;
    }

    public void setOwnLock(boolean ownLock) {
        this.ownLock = ownLock;
    }

    private void fail(DBVersionMeta dbVersionMeta, SQLException ex) {
        throw new HaltedException(
            "Could not require busy-lock '" + BUSY_VERSION + "' from table '" + dbVersionMeta.getTableName() +
                "'. " +
                "Perhaps another instance of dbmigrate is currently running on the database or " +
                "the lock was not correctly removed from a previous execution of dbmigrate. " +
                "\nTo remove the lock, execute: " +
                "DELETE FROM " + dbVersionMeta.getTableName() + " WHERE " + dbVersionMeta.getColumn_version() +
                " = '" + BUSY_VERSION + "';", ex);
    }

    private void waitAndRetry(DBVersionMeta dbVersionMeta, JdbcDatabase database, int attempt, SQLException ex) {
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
                tryLock(dbVersionMeta, database);
                return;
            } catch (SQLException e) {
                ex = e;
                attempt++;
            }
        }

        fail(dbVersionMeta, ex);
    }

    public void unlockBusy(DBVersionMeta dbVersionMeta, JdbcDatabase database) {
        if (dbVersionMeta.getLockBusy() == DBVersionMeta.LockBusy.No) return;

        if (!isOwnLock()) {
            log.info("Not deleting lock '" + BUSY_VERSION + "' on table " + dbVersionMeta.getTableName() +
                " because this instance does not own it.");
        } else {
            try {
                int count = UpdateVersionScriptVisitor.deleteVersion(database, BUSY_VERSION, dbVersionMeta);
                if (count != 1) {
                    log.warn(
                        dbVersionMeta.toSQLDelete() + " for busy-lock '" + BUSY_VERSION + "' affected " + count +
                            " rows!");
                } else {
                    log.info(
                        "Deleted busy-lock '" + BUSY_VERSION + "' on table " + dbVersionMeta.getTableName());
                    setOwnLock(false);
                }
            } catch (SQLException e) {
                log.error("Failed to delete busy-lock", e);
            }
        }
    }

}
