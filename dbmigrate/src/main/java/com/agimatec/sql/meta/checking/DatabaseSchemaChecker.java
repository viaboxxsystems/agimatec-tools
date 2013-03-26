package com.agimatec.sql.meta.checking;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.jdbc.JdbcException;
import com.agimatec.sql.meta.*;
import com.agimatec.sql.meta.mysql.MySqlSchemaChecker;
import com.agimatec.sql.meta.oracle.OracleSchemaChecker;
import com.agimatec.sql.meta.postgres.PostgresSchemaChecker;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 24.04.2007 <br/>
 * Time: 14:58:47 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class DatabaseSchemaChecker {
    protected static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaChecker.class);
    protected final List myFoundErrors;
    protected JdbcDatabase database;
    protected Map unknownColumns;    // key = table name, value = set of column names

    public static DatabaseSchemaChecker forDbms(String dbms) {
        if (dbms.equalsIgnoreCase("oracle")) {
            return new OracleSchemaChecker();
        } else if (dbms.equalsIgnoreCase("postgres")) {
            return new PostgresSchemaChecker();
        } else if (dbms.equalsIgnoreCase("mysql")) {
            return new MySqlSchemaChecker();
        }
        return null;
    }

    public DatabaseSchemaChecker() {
        myFoundErrors = new ArrayList();
    }

    public JdbcDatabase getDatabase() {
        return database;
    }

    public void setDatabase(JdbcDatabase aDatabase) {
        database = aDatabase;
    }

    /**
     * configuration - declare a column as not mapped in the database, so that
     * it will not be treated as an error during assertSchemaComplete().
     *
     * @param tableName
     * @param columnName
     */
    public void addUnmappedColumn(String tableName, String columnName) {
        Collection c = (Collection) getUnmappedColumns().get(tableName.toUpperCase());
        if (c == null) {
            c = new HashSet();

            getUnmappedColumns().put(tableName.toUpperCase(), c);
        }

        c.add(columnName);
    }

    /**
     * @return a Map (read-write)
     */
    public Map getUnmappedColumns() {
        if (unknownColumns == null) {
            unknownColumns = new HashMap();
        }

        return unknownColumns;
    }

    /**
     * @param tableName
     * @return a collection (read-only)
     */
    public Collection getUnmappedColumns(String tableName) {
        Collection c = (Collection) getUnmappedColumns().get(tableName.toUpperCase());
        if (c == null) {
            return Collections.EMPTY_SET;
        } else {
            return c;
        }
    }

    protected void throwAssertions() {
        if (!myFoundErrors.isEmpty()) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < myFoundErrors.size(); i++) {
                String s = (String) myFoundErrors.get(i);
                buf.append(i + 1).append("> ").append(s).append("\n\r");
            }
            throw new JdbcException(buf.toString());
        }
    }

    /**
     * API - check for invalid objects in the database
     *
     * @throws Exception
     */
    public abstract void assertObjectsValid() throws Exception;

    /**
     * API - check the database for compatibility with the given XML-DDL configuration.
     * Additional add all DDL in the scripts to the schema.
     *
     * @param scripts - scripts for schema (Soll-Zustand)
     *                Der Ist-Zustand steht in der Datenbank und wird mit dem Soll-Zustand verglichen.
     * @throws Exception
     */
    public void checkDatabaseSchema(URL[] scripts) throws Exception {
        CatalogDescription expectedCatalog;
        DDLScriptSqlMetaFactory factory = getDDLScriptSqlMetaFactory();
        for (URL script : scripts) {
            factory.fillCatalog(script);
        }
        expectedCatalog = factory.getCatalog();
        if (expectedCatalog == null) {
            assertTrue("No expected Catalog: neither schemaconfig nor scripts given!",
                    false);
            throwAssertions();
        } else {
            CatalogDescription databaseCatalog =
                    readDatabaseCatalog(expectedCatalog.getTableNames());
            print("Checking Database Schema " + databaseCatalog.getSchemaName() + "..");
            assertCatalogsComplete(expectedCatalog, databaseCatalog);
            print("Schema : Check OK");
        }
    }

    protected abstract DDLScriptSqlMetaFactory getDDLScriptSqlMetaFactory();


    protected abstract CatalogDescription readDatabaseCatalog(String[] tableNames) throws
            SQLException, IOException;

    public void assertCatalogsComplete(CatalogDescription schemaConfig, CatalogDescription databaseConfig) {
        myFoundErrors.clear();
        String[] tables = schemaConfig.getTableNames();
        for (String theTable : tables) {
            TableDescription xmlTableDescription = schemaConfig.getTable(theTable);
            TableDescription databaseTableDescription = databaseConfig
                    .getTable(xmlTableDescription.getTableName().toUpperCase());
            if (databaseTableDescription != null) {
                log("Checking " + databaseTableDescription.getTableName() + "...");
                compareSingleIndexDescription(xmlTableDescription.getPrimaryKey(),
                        databaseTableDescription.getPrimaryKey());
                compareColumnDescription(xmlTableDescription, databaseTableDescription);
                compareIndexDescription(xmlTableDescription, databaseTableDescription);
                compareForeignKeyDescription(xmlTableDescription,
                        databaseTableDescription);
                checkUnknownColumns(databaseTableDescription,
                        xmlTableDescription.getColumnNames());
            } else assertTrue("Table: " + xmlTableDescription.getTableName() +
                    "... not found in databaseCatalog!", false);
        }
        // TODO RSt - views checking not yet implemented
        // todo [RSt] sequences not yet implemented  -> requires DDLScriptSqlMetaFactory
        // todo [RSt] function based indices not yet implemented  -> requires DDLScriptSqlMetaFactory
        // todo [RSt] missing indexes/foreignkeys in schemaConfig not detected -> requires DDLScriptSqlMetaFactory
        throwAssertions();
    }

    /**
     * @param databaseTableDescription - table for columns actually in the database
     * @param configColumns            - column names of configurated columns
     */
    private void checkUnknownColumns(TableDescription databaseTableDescription, String[] configColumns) {
        Collection<String> c =
                getUnmappedColumns(databaseTableDescription.getTableName());
        String[] unmappedColumns = c.toArray(new String[0]);
        // check that unmapped columns exist in database
        for (String unmapped : c) {
            assertTrue(databaseTableDescription.getTableName() + "." + unmapped +
                    " [declared as unknown, but] not in database",
                    databaseTableDescription.getColumn(unmapped) != null);
        }
        String[] dbColumns = databaseTableDescription.getColumnNames();
        for (String theDbColumn : dbColumns) {
            if (!containsIgnoreCase(unmappedColumns, theDbColumn)) {
                assertTrue("Table " + databaseTableDescription.getTableName() +
                        " contains unknown column: '" + theDbColumn + "'",
                        containsIgnoreCase(configColumns, theDbColumn));
            }
        }
    }

    private boolean containsIgnoreCase(String[] arr, String val) {
        for (String each : arr) {
            if (val.equalsIgnoreCase(each)) return true;
        }
        return false;
    }

    protected void compareSingleIndexDescription(IndexDescription xmlIndexDescription, IndexDescription databaseIndexDescription) {
        if (xmlIndexDescription == null && databaseIndexDescription == null) return;
        if (xmlIndexDescription == null) {
            assertTrue("Table: " + databaseIndexDescription.getTableName() +
                    "... IndexName not expected! " +
                    databaseIndexDescription.getIndexName() + " databaseIndexName: " +
                    databaseIndexDescription.getIndexName(), false);
            return;
        }
        if (databaseIndexDescription == null) {
            assertTrue("Table: " + xmlIndexDescription.getTableName() +
                    "...DatabaseIndexName not found!! Expected IndexName: " +
                    xmlIndexDescription.getIndexName(), false);
            return;
        }
        if (xmlIndexDescription.isFunctionBased() ||
                databaseIndexDescription.isFunctionBased()) {
            log("function based not yet supported"); // todo [RSt] nyi
            return;
        }
        if (xmlIndexDescription.getIndexName() == null || xmlIndexDescription
                .getIndexName()
                .equalsIgnoreCase(databaseIndexDescription.getIndexName())) {
            boolean columnsOK = indexColumnsEqual(xmlIndexDescription,
                    databaseIndexDescription);
            if (!columnsOK) {
                assertTrue("Table: " + xmlIndexDescription.getTableName() +
                        ", index: " + xmlIndexDescription.getIndexName() +
                        "... Columns differ! expected: " +
                        xmlIndexDescription.getColumns() + " but was " +
                        databaseIndexDescription.getColumns(), false);
            }
        } else {
            assertTrue("Table: " + xmlIndexDescription.getTableName() +
                    "... Wrong Indexname! Expected IndexName: " +
                    xmlIndexDescription.getIndexName() + " databaseIndexName: " +
                    databaseIndexDescription.getIndexName(), false);
        }
    }

    private boolean indexColumnsEqual(IndexDescription xmlIndexDescription, IndexDescription databaseIndexDescription) {
        boolean columnsOK = xmlIndexDescription.getColumnSize() ==
                databaseIndexDescription.getColumnSize();
        if (columnsOK) {
            for (int i = 0; i < xmlIndexDescription.getColumnSize(); i++) {
                String xmlColumn = xmlIndexDescription.getColumn(i);
                String dbColumn = databaseIndexDescription.getColumn(i);
                if (xmlColumn.equalsIgnoreCase(dbColumn)) {
                    assertTrue("Table: " + xmlIndexDescription.getTableName() +
                            "... Wrong Orderdirection! Column:" +
                            xmlIndexDescription.getColumn(i) +
                            " expected OrderDirection: " +
                            xmlIndexDescription.getOrderDirection(i) +
                            " databaseOrderDirection: " +
                            databaseIndexDescription.getOrderDirection(i),
                            xmlIndexDescription.getOrderDirection(i) ==
                                    databaseIndexDescription.getOrderDirection(i));
                } else {
                    columnsOK = false;
                }
            }
        }
        return columnsOK;
    }

    protected void compareIndexDescription(TableDescription xmlTableDescription, TableDescription databaseTableDescription) {
        for (int i = 0; i < xmlTableDescription.getIndexSize(); i++) {
            IndexDescription xmlIndexDescription = xmlTableDescription.getIndex(i);
            IndexDescription databaseIndexDescription = null;
            if (xmlIndexDescription.getIndexName() != null) {
                databaseIndexDescription =
                        databaseTableDescription.getIndex(xmlIndexDescription.getIndexName());
            } else {
                for (IndexDescription each : databaseTableDescription.getIndices()) {
                    if (indexColumnsEqual(xmlIndexDescription, each)) {
                        databaseIndexDescription = each;
                        break;
                    }
                }
                if (databaseIndexDescription == null)
                    databaseIndexDescription = databaseTableDescription.getPrimaryKey();
            }
            compareSingleIndexDescription(xmlIndexDescription, databaseIndexDescription);
        }
    }

    private void compareColumnDescription(TableDescription xmlTableDescription, TableDescription databaseTableDescription) {
        String tableName = xmlTableDescription.getTableName();
        for (int i = 0; i < xmlTableDescription.getColumnSize(); i++) {
            ColumnDescription xmlColumnDescription = xmlTableDescription.getColumn(i);
            ColumnDescription databaseColumnDescription = databaseTableDescription
                    .getColumn(xmlColumnDescription.getColumnName());
            assertTrue(xmlTableDescription + "." + xmlColumnDescription + " not in database.",
                    databaseColumnDescription != null);
            if (databaseColumnDescription != null) {
                assertTrue("Table: " + tableName + ", ColumnName: " +
                        xmlColumnDescription.getColumnName() +
                        "... Wrong Precision! Expected Precision: " +
                        xmlColumnDescription.getPrecision() + " databasePrecision: " +
                        databaseColumnDescription.getPrecision(),
                        isPrecisionCompatible(xmlColumnDescription, databaseColumnDescription));

                assertTrue("Table: " + tableName + ", ColumnName: " +
                        xmlColumnDescription.getColumnName() +
                        "... Wrong Scale! Expected Scale: " +
                        xmlColumnDescription.getScale() + " databaseScale: " +
                        databaseColumnDescription.getScale(),
                        isScaleCompatible(xmlColumnDescription, databaseColumnDescription));

                assertTrue("Table: " + tableName + ", ColumnName: " +
                        xmlColumnDescription.getColumnName() +
                        "... Wrong Type! Expected Type: " +
                        xmlColumnDescription.getTypeName() + " databaseType: " +
                        databaseColumnDescription.getTypeName(),
                        isTypeCompatible(xmlColumnDescription, databaseColumnDescription));

                assertTrue("Table: " + tableName + ", ColumnName: " +
                        xmlColumnDescription.getColumnName() + "... Nullable expected: " +
                        xmlColumnDescription.isNullable() + " but was: " +
                        databaseColumnDescription.isNullable(), xmlColumnDescription
                        .isNullable() == databaseColumnDescription.isNullable());
            }
        }
    }

    protected boolean isTypeCompatible(ColumnDescription xmlColumnDescription, ColumnDescription databaseColumnDescription) {
        return xmlColumnDescription.getTypeName().equalsIgnoreCase(databaseColumnDescription.getTypeName());
    }

    protected boolean isScaleCompatible(ColumnDescription xmlColumnDescription, ColumnDescription databaseColumnDescription) {
        return xmlColumnDescription.getScale() == databaseColumnDescription.getScale();
    }

    protected boolean isPrecisionCompatible(ColumnDescription xmlColumnDescription, ColumnDescription databaseColumnDescription) {
        return xmlColumnDescription.getPrecision() == databaseColumnDescription.getPrecision();
    }

    protected void compareForeignKeyDescription(TableDescription xmlTableDescription, TableDescription databaseTableDescription) {
        String tableName = xmlTableDescription.getTableName();
        Set<ForeignKeyDescription> unCheckedDatabaseFKs = new HashSet<ForeignKeyDescription>();
        if (databaseTableDescription.getForeignKeys() != null) {
            unCheckedDatabaseFKs.addAll(databaseTableDescription.getForeignKeys());
        }
        for (int i = 0; i < xmlTableDescription.getForeignKeySize(); i++) {
            ForeignKeyDescription xmlForeignKeyDescription =
                    xmlTableDescription.getForeignKey(i);
            if (xmlForeignKeyDescription.getConstraintName() == null ||
                    xmlForeignKeyDescription.getConstraintName().length() == 0) {
                log("cannot check unnamed foreign key in " +
                        xmlForeignKeyDescription.getTableName() + " referencing " +
                        xmlForeignKeyDescription.getRefTableName());
                continue;
            }
            ForeignKeyDescription databaseForeignKeyDescription = databaseTableDescription
                    .getForeignKey(xmlForeignKeyDescription.getConstraintName());
            unCheckedDatabaseFKs.remove(databaseForeignKeyDescription);
            if (databaseForeignKeyDescription != null) {
                assertTrue("Table: " + tableName +
                        "... Wrong ConstraintName! Expected ConstraintName: " +
                        xmlForeignKeyDescription.getConstraintName() +
                        " databaseConstraintName: " +
                        databaseForeignKeyDescription.getConstraintName(),
                        xmlForeignKeyDescription.getConstraintName()
                                .equalsIgnoreCase(
                                        databaseForeignKeyDescription.getConstraintName()));

                assertTrue("Table: " + tableName + ", ConstraintName: " +
                        xmlForeignKeyDescription.getConstraintName() +
                        "... Wrong ReferencedTable! Expected RefTable: " +
                        xmlForeignKeyDescription.getRefTableName() +
                        " databaseRefTable: " +
                        databaseForeignKeyDescription.getRefTableName(),
                        xmlForeignKeyDescription.getRefTableName().equalsIgnoreCase(
                                databaseForeignKeyDescription.getRefTableName()));

                for (int j = 0; j < xmlForeignKeyDescription.getColumnSize(); j++) {
                    String xmlColumn = xmlForeignKeyDescription.getColumn(j);
                    String xmlRefColumn = xmlForeignKeyDescription.getRefColumn(j);
                    assertTrue("Table: " + tableName + ", ConstraintName: " +
                            xmlForeignKeyDescription.getConstraintName() +
                            "... Column not found! Expected Column: " + xmlColumn,
                            databaseForeignKeyDescription.getColumn(xmlColumn) != -1);
                    if (xmlRefColumn != null) {
                        assertTrue("Table: " + tableName + ", ConstraintName: " +
                                xmlForeignKeyDescription.getConstraintName() +
                                "... ReferencedColumn not found! Expected Column: " +
                                xmlRefColumn, databaseForeignKeyDescription
                                .getRefColumn(xmlRefColumn) != -1);
                    }
                }
            } else assertTrue("Table: " + tableName +
                    "... ConstraintName not found! Expected ConstraintName: " +
                    xmlForeignKeyDescription.getConstraintName(), false);
        }
        for (ForeignKeyDescription uncheckedFK : unCheckedDatabaseFKs) {
            assertTrue("Table: " + tableName + " contains unexpected foreign key constaint named '"
                    + uncheckedFK + " on columns " + uncheckedFK.getColumns() + "' referencing table '" + uncheckedFK.getRefTableName() + "'", false);
        }
    }

    protected void assertTrue(String s, boolean b) {
        if (!b) {
            myFoundErrors.add(s);
        }
    }


    /**
     * log INFO and print to console
     */
    protected void print(Object obj) {
        log(obj);
        System.out.println(obj);
    }

    /**
     * log INFO or Exception
     *
     * @param obj
     */
    protected void log(Object obj) {
        if (logger != null) {
            if (obj instanceof Throwable) {
                logger.error(null, (Throwable) obj);
            } else {
                logger.info(String.valueOf(obj));
            }
        }
    }
}
