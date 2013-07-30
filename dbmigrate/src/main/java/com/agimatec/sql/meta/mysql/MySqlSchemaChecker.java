package com.agimatec.sql.meta.mysql;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.checking.DatabaseSchemaChecker;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.sql.SQLException;

/**
 * <p>check a mysql catalog for validity</p>
 * User: roman.stumm@viaboxx.de<br>
 * Date: 26.03.13
 */
public class MySqlSchemaChecker extends DatabaseSchemaChecker {
    @Override
    public void assertObjectsValid() throws Exception {
        // do nothing
    }

    @Override
    protected DDLScriptSqlMetaFactory getDDLScriptSqlMetaFactory() {
        return new DDLScriptSqlMetaFactory(
                DDLExpressions.forDbms("mysql"));
    }

    @Override
    protected CatalogDescription readDatabaseCatalog(String[] tableNames) throws SQLException, IOException {
        MySqlJdbcMetaFactory factory = new MySqlJdbcMetaFactory(getDatabase());
        return factory.buildCatalog(tableNames);
    }

    protected boolean isPrecisionCompatible(ColumnDescription expected, ColumnDescription actual) {
        int xmlPrecision = expected.getPrecision();
        // if script type does not contain precision, add the default precision, because the jdbc-driver will return it.
        if (xmlPrecision == 0 && "INT".equalsIgnoreCase(expected.getTypeName()))
            xmlPrecision = 11; // Precision 11 is default for INT
        else if (xmlPrecision == 0 && "INT UNSIGNED".equalsIgnoreCase(expected.getTypeName()))
            xmlPrecision = 10; // Precision 10 is default for INT UNSIGNED
        else if (xmlPrecision == 0 && "BIGINT".equalsIgnoreCase(expected.getTypeName()))
            xmlPrecision = 20; // Precision 20 is default for BIGINT
        else if (xmlPrecision == 0 && "SMALLINT".equalsIgnoreCase(expected.getTypeName()))
            xmlPrecision = 6;  // Precision 6 is default for SMALLINT
        else if (xmlPrecision == 0 && "DOUBLE".equalsIgnoreCase(expected.getTypeName())) xmlPrecision = 22;
        else if (xmlPrecision == 0 && "TEXT".equalsIgnoreCase(expected.getTypeName())) xmlPrecision = 65535;
        else if (xmlPrecision == 0 && "MEDIUMTEXT".equalsIgnoreCase(expected.getTypeName())) xmlPrecision = 16777215;
        else if (xmlPrecision == 0 && "MEDIUMBLOB".equalsIgnoreCase(expected.getTypeName())) xmlPrecision = 16777215;
        else if (xmlPrecision == 0 && "BLOB".equalsIgnoreCase(expected.getTypeName())) xmlPrecision = 65535;
        else if (xmlPrecision == 0 && "FLOAT".equalsIgnoreCase(expected.getTypeName()))
            xmlPrecision = 12;  // Precision 12 is default for FLOAT
        else if (xmlPrecision == 0 && "LONGBLOB".equalsIgnoreCase(expected.getTypeName()))
            xmlPrecision = 2147483647;  // Precision 2147483647 is default for LONGBLOB
        return xmlPrecision == actual.getPrecision() || (
                ArrayUtils.contains(MySqlJdbcMetaFactory.NUM_TYPES, actual.getTypeName().toUpperCase()) &&
                        actual.getPrecision() >
                                xmlPrecision // wrong precision returned by driver int(8) ==> precision = 10
        );
    }
}
