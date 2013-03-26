package com.agimatec.sql.meta.mysql;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.checking.DatabaseSchemaChecker;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;

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

    protected boolean isPrecisionCompatible(ColumnDescription xmlColumnDescription, ColumnDescription databaseColumnDescription) {
        int xmlPrecision = xmlColumnDescription.getPrecision();
        // if script type does not contain precision, add the default precision, because the jdbc-driver will return it.
        if (xmlPrecision == 0 && "BIGINT".equals(xmlColumnDescription.getTypeName())) xmlPrecision = 20;
        if (xmlPrecision == 0 && "SMALLINT".equals(xmlColumnDescription.getTypeName())) xmlPrecision = 6;
        if (xmlPrecision == 0 && "DOUBLE".equals(xmlColumnDescription.getTypeName())) xmlPrecision = 22;
        if (xmlPrecision == 0 && "TEXT".equals(xmlColumnDescription.getTypeName())) xmlPrecision = 65535;
        return xmlPrecision == databaseColumnDescription.getPrecision();
    }
}
