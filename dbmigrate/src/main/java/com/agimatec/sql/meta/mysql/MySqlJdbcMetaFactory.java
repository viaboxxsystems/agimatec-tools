package com.agimatec.sql.meta.mysql;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.checking.JdbcSqlMetaFactory;

/**
 * <p>read mysql catalog information</p>
 * User: roman.stumm@viaboxx.de<br>
 * Date: 26.03.13
 */
public class MySqlJdbcMetaFactory extends JdbcSqlMetaFactory {
    public MySqlJdbcMetaFactory(JdbcDatabase database) {
        super(database);
    }

    @Override
    /**
     * equalize type names: jdbcdriver -> sql-script-type-name
     * @param cd - contains the type name returned by the JdbcDriver
     **/
    public void equalizeColumn(ColumnDescription cd) {
        super.equalizeColumn(cd);
        if (cd.getTypeName().equals("BIGINT")) {
            cd.setPrecision(cd.getPrecision() + 1);  // driver returns 19 for BIGINT(20)
            // Precision 20 is default for BIGINT
        } else if (cd.getTypeName().equals("SMALLINT")) {
            cd.setPrecision(cd.getPrecision() + 1);  // driver returns 5 for SMALLINT(6)
            // Precision 6 is default for SMALLINT
        }
    }

    @Override
    protected TableIdentifier createTableIdentifier(String table) {
        return super.createTableIdentifier(table.toLowerCase());
    }
}
