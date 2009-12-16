package com.agimatec.dbmigrate.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public class SQLCursor extends ResultSetDelegate {
    private Statement statement;

    public SQLCursor(Statement aStmt, ResultSet aResultSet) {
        super(aResultSet);
        statement = aStmt;
    }

    public void close() throws SQLException {
        super.close();

        if (getStatement() != null) getStatement().close();
    }

    public Statement getStatement() {
        return statement;
    }
}


