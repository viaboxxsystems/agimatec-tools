package com.agimatec.dbmigrate.util;

import junit.framework.TestCase;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 30.03.2010<br>
 * Time: 10:08:02<br>
 * viaboxx GmbH, 2010
 */
public class DBVersionMetaTest extends TestCase {
    public void testCreate() {
        DBVersionMeta d = new DBVersionMeta();
        assertEquals("CREATE TABLE DB_VERSION (since TIMESTAMP, version VARCHAR(100) NOT NULL, PRIMARY KEY (version)",
            d.toSQLCreateTable());
        d.setColumn_since(null);
        assertEquals("CREATE TABLE DB_VERSION (version VARCHAR(100) NOT NULL, PRIMARY KEY (version)",
            d.toSQLCreateTable());
    }

    public void testCreateInsertOnly() {
        DBVersionMeta d = new DBVersionMeta();
        d.setInsertOnly(true);
        assertEquals("CREATE TABLE DB_VERSION (since TIMESTAMP, version VARCHAR(100) NOT NULL)",
            d.toSQLCreateTable());
        d.setColumn_since(null);
        assertEquals("CREATE TABLE DB_VERSION (version VARCHAR(100) NOT NULL)",
            d.toSQLCreateTable());
    }

    public void testCreateLockTable() {
        DBVersionMeta d = new DBVersionMeta();
        d.setTableName(d.getLockTableName());
        d.setInsertOnly(false);
        assertEquals("CREATE TABLE DB_MIGLOCK (since TIMESTAMP, version VARCHAR(100) NOT NULL, PRIMARY KEY (version)",
            d.toSQLCreateTable());
        d.setColumn_since(null);
        assertEquals("CREATE TABLE DB_MIGLOCK (version VARCHAR(100) NOT NULL, PRIMARY KEY (version)",
            d.toSQLCreateTable());
    }
}
