package com.agimatec.sql.meta.postgres;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.checking.DatabaseSchemaChecker;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 24.04.2007 <br/>
 * Time: 15:03:12 <br/>
 * Copyright: Agimatec GmbH
 */
public class PostgresSchemaChecker extends DatabaseSchemaChecker {
    /**
     * API - check for invalid objects in the database
     *
     * @throws Exception
     */
    public void assertObjectsValid() throws Exception {
        // do nothing (indexes, views, triggers, ... - can they become invalid, how to check this?)
    }

    protected DDLScriptSqlMetaFactory getDDLScriptSqlMetaFactory() {
        return new DDLScriptSqlMetaFactory(
                DDLExpressions.forDbms("postgres"));
    }

    protected CatalogDescription readDatabaseCatalog(String[] tableNames)
            throws SQLException, IOException {
        PostgresJdbcSqlMetaFactory factory = new PostgresJdbcSqlMetaFactory(getDatabase());
        return factory.buildCatalog(tableNames);
    }

    @Override
    protected boolean isTypeCompatible(ColumnDescription expected, ColumnDescription actual) {
        boolean valid = super.isTypeCompatible(expected, actual);    // call super!
        if (valid) return valid;
        return equalizeType(expected).equalsIgnoreCase(equalizeType(actual));
    }

    private String equalizeType(ColumnDescription cd) {
        if (cd.getTypeName().equalsIgnoreCase("float8")) {
            return "FLOAT";
        } else if (cd.getTypeName().equalsIgnoreCase("numeric")) {
            return "DECIMAL";
        }
        return cd.getTypeName();
    }
}
