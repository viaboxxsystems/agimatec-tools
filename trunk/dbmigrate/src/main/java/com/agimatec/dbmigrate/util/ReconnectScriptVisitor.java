package com.agimatec.dbmigrate.util;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.jdbc.JdbcException;
import com.agimatec.sql.script.ScriptVisitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <b>Description:</b>   script visitor with JdbcDatabase supporting reconnect<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 */
public class ReconnectScriptVisitor extends ScriptVisitorDelegate {
    private static final Log log = LogFactory.getLog(ReconnectScriptVisitor.class);

    private JdbcDatabase store;

    public ReconnectScriptVisitor(JdbcDatabase targetStore, ScriptVisitor visitor) {
        super(visitor);
        this.store = targetStore;
    }

    public int visitStatement(String statement) throws SQLException {

        if (statement.toUpperCase().startsWith("CONNECT ")) {
            try {
                reconnect(statement);

                return 0;
            } catch (JdbcException ex) {
                log.debug("error during reconnect()", ex);
                throw new SQLException(ex.getMessage());
            }
        } else {
            return super.visitStatement(statement);
        }
    }

    private void reconnect(String connectCmd) {
        reconnect(store, connectCmd);
    }

    /**
     * @param connectCmd
     */
    public static void reconnect(JdbcDatabase store, String connectCmd) {
        if (log.isInfoEnabled()) {
            log.info(connectCmd);
        }
        String url;
        String driver = store.getDriverClassName();
        Properties props = new Properties(store.getProperties());
        StringTokenizer tokens = new StringTokenizer(connectCmd, " /@", false);

        tokens.nextToken();     // skip "CONNECT"
        String user = tokens.nextToken();
        String pw = tokens.nextToken();
        props.put("user", user);
        props.put("password", pw);

        int idx = connectCmd.indexOf('@');
        if (idx >= 0) {
            url = connectCmd.substring(connectCmd.indexOf('@') + 1).trim();
            if (connectCmd.indexOf(':') <
                    0) { // format: "CONNECT USER/PASSWORD@SCHEMANAME"
                url = replaceJdbcSchemaName(store.getConnectionString(), url);
            }
        } else {
            url = null;
        }
        if (store.isTransaction()) {
            store.commit();
        }
        store.close();
        store.init(driver, url, props);
        store.begin();
    }

    public static String replaceJdbcSchemaName(String jdbcUrl, String schemaname) {
        if (jdbcUrl == null || jdbcUrl.endsWith("/")) {
            return jdbcUrl + schemaname;
        } else {
            return jdbcUrl.substring(0, jdbcUrl.lastIndexOf('/') + 1) + schemaname;
        }
    }

    public static String replaceTNSName(String jdbcUrl, String schemaname) {
        String url = jdbcUrl;
        url = url.substring(0, url.indexOf('@') + 1);
        url = url + schemaname;
        return url;
    }
}


