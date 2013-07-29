package com.agimatec.sql.meta.mysql;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.checking.JdbcSqlMetaFactory;
import org.apache.commons.lang.ArrayUtils;

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
        if(ArrayUtils.contains(NUM_TYPES, cd.getTypeName().toUpperCase())) {
            cd.setPrecision(cd.getPrecision() + 1); // driver returns a precision that is one to low (19 for "BIGINT(20)" etc.)
        }
        if(cd.getTypeName().toUpperCase().equals("TINYINT UNSIGNED")) {
            cd.setPrecision(cd.getPrecision() - 1); // driver returns a precision that is one to high (3 for "tinyint(2) unsigned" etc.)
        }
    }

    static final String[] NUM_TYPES = {"BIGINT", "SMALLINT", "INT", "TINYINT"};

    @Override
    protected TableIdentifier createTableIdentifier(String table) {
        return super.createTableIdentifier(table.toLowerCase());
    }
}
