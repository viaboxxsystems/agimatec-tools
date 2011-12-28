package com.agimatec.database;

import com.agimatec.sql.query.JdbcQueryUtil;
import com.agimatec.sql.query.QueryDefinition;
import com.agimatec.sql.query.QueryResult;
import com.agimatec.sql.query.SQLBuilder;
import com.agimatec.utility.fileimport.SqlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

/**
 * Description: Löscht alle functions und triggers aus der datenbank,
 * die - nach Namenskonvention - fuer Historisierung zustaendig sind.<br/>
 * User: roman.stumm <br/>
 * Date: 09.11.2007 <br/>
 * Time: 15:54:36 <br/>
 * Copyright: Agimatec GmbH
 */
public class DeleteHistoryTriggers extends AbstractDbTool {
    protected static final Log log = LogFactory.getLog("agimatec.migration");

    public static void main(String[] args) throws Exception {
        DeleteHistoryTriggers tool = new DeleteHistoryTriggers();
        try {
            tool.connectJdbc(args);
            try {
                tool.execute();
            } finally {
                tool.disconnect();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute() throws IOException, SQLException {
        final SQLBuilder sqlBuilder;
        if(SqlUtil.forConnection(jdbcConnection).isOracle()) {
            sqlBuilder = new SQLBuilder(
                    "com/agimatec/sql/meta/oracle-statements.properties");
        } else {
            sqlBuilder = new SQLBuilder(
                    "com/agimatec/sql/meta/postgres-statements.properties");
        }
        JdbcQueryUtil queryUtil = new JdbcQueryUtil(jdbcConnection, sqlBuilder);
        QueryDefinition def = new QueryDefinition();
        def.setQueryName("history-triggers");
        QueryResult<String> triggers = queryUtil.executeQuery(def);
        Statement stmt = jdbcConnection.createStatement();
        try {
            for (String each : triggers) {
                String sql = queryUtil.getSqlBuilder().getSQL("drop-trigger");
                sql = MessageFormat.format(sql, each, each.substring(5));
                log.info(sql);
                stmt.execute(sql);
            }

            def.setQueryName("history-trigger-functions");
            QueryResult<String> functions = queryUtil.executeQuery(def);
            for (String each : functions) {
                String sql = queryUtil.getSqlBuilder().getSQL("drop-function");
                sql = MessageFormat.format(sql, each);
                log.info(sql);
                stmt.execute(sql);
            }
        } finally {
            stmt.close();
        }
    }
}
