package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.checking.DatabaseSchemaChecker;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Description: Utility class to check if a given database schema
 * is compatible with a physical database schema:
 * - all tables + columns that are mapped are in the database
 * - no unknown columns are in the database
 * - etc.
 * </p>
 * User: roman.stumm <br/>
 * Date: 24.04.2007 <br/>
 * Time: 14:44:15 <br/>
 * Copyright: Agimatec GmbH
 */
public class OracleSchemaChecker extends DatabaseSchemaChecker {
    /**
     * API -
     *
     * @throws Exception
     */
    public void assertObjectsValid() throws Exception {
        assertIndexValid();
        assertTriggersValid();
        assertViewsValid();
    }

    protected DDLScriptSqlMetaFactory getDDLScriptSqlMetaFactory() {
        return new DDLScriptSqlMetaFactory(DDLExpressions.forDbms("oracle"));
    }

    protected CatalogDescription readDatabaseCatalog(String[] tableNames)
            throws SQLException, IOException {
        OracleJdbcSqlMetaFactory oracleFactory = new OracleJdbcSqlMetaFactory(getDatabase());
        return oracleFactory.buildCatalog(tableNames);
    }

    /**
     * check if the views in the database are valid.
     * if not, throw assertion error with invalid views.
     * Caution: This methods starts an own transaction and finally commits it!
     *
     * @throws Exception
     */
    public void assertViewsValid() throws Exception {
        myFoundErrors.clear();
        assertObjectsValid("VIEW");
        throwAssertions();
    }

    public void assertIndexValid() throws Exception {
        myFoundErrors.clear();
        assertObjectsValid("INDEX");
        throwAssertions();
    }

    /**
     * API - check if the triggers in the database are valid.
     * if not, throw assertion erroor with invalid trigger name.
     * Caution: This method start an own transaction and finally commits it!
     *
     * @throws Exception
     */
    public void assertTriggersValid() throws Exception {
        myFoundErrors.clear();
        assertObjectsValid("TRIGGER");
        throwAssertions();
    }

    /**
     * Caution: Runs in own transaction, commits afterwards!
     *
     * @param objectType - oracle object type e.g. "TRIGGER", "VIEW"
     * @throws Exception
     */
    protected void assertObjectsValid(String objectType) throws Exception {
        print("Checking " + objectType + "..");
        java.sql.Connection conn = getDatabase().getConnection();
        List invalidObjects = getInvalidObjects(objectType);
        if (!invalidObjects.isEmpty()) {  // cannot compile index
            Iterator iter = invalidObjects.iterator();
            if (!objectType.equalsIgnoreCase("INDEX")) {
                while (iter.hasNext()) {
                    String obj = (String) iter.next();
                    compileObject(conn, obj, objectType);
                }
                invalidObjects = getInvalidObjects(objectType); // try again
                iter = invalidObjects.iterator();
            }
            StringBuilder buf = new StringBuilder();
            buf.append("Invalid ").append(objectType).append(" detected: ");
            while (iter.hasNext()) {
                String obj = (String) iter.next();
                buf.append(obj);
                if (iter.hasNext()) buf.append(", ");
            }
            assertTrue(buf.toString(), invalidObjects.isEmpty());
        }
        print(objectType + " checked.");
    }

    /**
     * @param aView      - the name of the trigger or view
     * @param objecttype - the oracle objecttype e.g. "TRIGGER", "VIEW"
     * @throws SQLException
     */
    private void compileObject(Connection aConn, String aView, String objecttype)
            throws SQLException {
        Statement stmt = aConn.createStatement();
        try {
            stmt.execute("ALTER " + objecttype + " " + aView + " COMPILE"); // try to recompile now
        } finally {
            stmt.close();
        }
    }

    /**
     * @param objectType - the oracle object type name, e.g. "VIEW", "TRIGER"
     * @return list of views/triggers names (String) that are currently invalid
     * @throws SQLException
     */
    private List getInvalidObjects(String objectType) throws SQLException {
        if (objectType.equalsIgnoreCase("INDEX")) return getInvalidIndex();

        String stmtInvalidObjects = "SELECT OBJECT_NAME FROM USER_OBJECTS WHERE OBJECT_TYPE = '" +
                objectType + "' AND STATUS != 'VALID' ORDER BY OBJECT_NAME";
        List invalidObjects = new ArrayList();
        Connection conn = getDatabase().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery(stmtInvalidObjects);
        try {
            while (resultSet.next()) {
                invalidObjects.add(resultSet.getString(1));
            }
        } finally {
            resultSet.close();
            stmt.close();
        }
        return invalidObjects;
    }

    /** @return list of views/triggers names (String) that are currently invalid */
    private List getInvalidIndex() throws SQLException {
        String stmtInvalidObjects =
                "select index_name, DOMIDX_STATUS from user_indexes where DOMIDX_OPSTATUS is not null " +
                        "and (DOMIDX_OPSTATUS <> 'VALID' or DOMIDX_STATUS   <> 'VALID')";
        List invalidObjects = new ArrayList();
        Connection conn = getDatabase().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery(stmtInvalidObjects);
        try {
            while (resultSet.next()) {
                invalidObjects.add(resultSet.getString(1));
            }
        } finally {
            resultSet.close();
            stmt.close();
        }
        return invalidObjects;
    }


}


