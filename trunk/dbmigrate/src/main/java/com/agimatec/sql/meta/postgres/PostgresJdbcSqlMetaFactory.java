package com.agimatec.sql.meta.postgres;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.checking.JdbcSqlMetaFactory;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 10.03.2008 <br/>
 * Time: 10:41:46 <br/>
 * Copyright: Agimatec GmbH
 */
public class PostgresJdbcSqlMetaFactory extends JdbcSqlMetaFactory {
    public PostgresJdbcSqlMetaFactory(JdbcDatabase database) {
        super(database);
    }

    @Override
    /**
     * equalize type names: jdbcdriver -> sql-script-type-name
     * @param cd - contains the type name returned by the JdbcDriver
     **/
    public void equalizeColumn(ColumnDescription cd) {
        super.equalizeColumn(cd);
        if (cd.getTypeName().equals("int8")) {
            cd.setTypeName("BIGINT");
        } else if (cd.getTypeName().equals("int4")) {
            cd.setTypeName("INTEGER");
        } else if (cd.getTypeName().equals("timestamp")) {
            cd.setPrecision(0);
            cd.setPrecisionEnabled(false);
        } else if (cd.getTypeName().equals("int2")) {
            cd.setTypeName("SMALLINT");
        } else if (cd.getTypeName().equals("bpchar")) {
            cd.setTypeName("CHARACTER");
        } else if (cd.getTypeName().equals("bool")) {
            cd.setTypeName("BOOLEAN");
        } else if (cd.getTypeName().equals("time")) {
            cd.setPrecisionEnabled(false);
            cd.setPrecision(0);
        }
    }

    @Override
    protected TableIdentifier createTableIdentifier(String table) {
        return super.createTableIdentifier(table.toLowerCase());
    }

}
