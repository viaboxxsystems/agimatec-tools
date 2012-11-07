package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.ForeignKeyDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ForeignKeyDescriptionBuilder implements JdbcResultBuilder {
    private static final int C_CONSTRAINT = 1;
    private static final int C_TABLE = 2;
    private static final int C_REF_TABLE = 3;
    private static final int C_COLUMN = 4;
    private static final int C_REF_COLUMN = 5;

    private List myFKs;
    private ForeignKeyDescription fk;

    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        myFKs = resultList;
    }

    public void fetch(ResultSet row) throws SQLException {
        String consName = row.getString(C_CONSTRAINT);
        if (fk == null || !(fk.getConstraintName().equals(consName))) {
            if (fk != null) addFK();
            fk = new ForeignKeyDescription();
            fk.setConstraintName(consName);
            fk.setTableName(row.getString(C_TABLE));
            fk.setRefTableName(row.getString(C_REF_TABLE));
        }
        fk.addColumnPair(row.getString(C_COLUMN), row.getString(C_REF_COLUMN));
    }

    public void close(boolean isComplete) throws SQLException {
        if (fk != null) addFK();
    }

    private void addFK() {
        myFKs.add(fk);
    }
}

