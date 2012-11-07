package com.agimatec.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 13:48:02 <br/>
 * Copyright: Agimatec GmbH
 */
class JdbcDatabaseImpl implements JdbcDatabase {
    protected static final Log log = LogFactory.getLog(JdbcDatabaseImpl.class);

    private Connection connection;
    private final Properties properties;
    private boolean transaction = false;
    private boolean autoCommit = false;

    public JdbcDatabaseImpl(Properties aProperties) {
        properties = new Properties();
        properties.putAll(aProperties);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getDriverClassName() {
        return properties.getProperty(JdbcConfig.JDBC_DRIVER);
    }

    public void begin() {
        if (isTransaction()) {
            throw new JdbcException("transaction already started");
        } else {
            if (connection == null) connect();
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        try {
            autoCommit = connection != null && connection.getAutoCommit();
        } catch (SQLException e) {
        }
        transaction = connection != null;
    }

    private void connect() {
        try {
            if (log.isInfoEnabled()) {
                log.info("JdbcDriver: " + getDriverClassName() + "; JdbcConnect: " +
                        getConnectionString() + "; properties: " + properties);
            }
            if (getDriverClassName() != null) Class.forName(getDriverClassName());
            setConnection(DriverManager.getConnection(getConnectionString(), properties));
//            connection.setAutoCommit(false);  // some database complains when using DDL statements in transacted mode
        } catch (Exception e) {
            throw new JdbcException(e);
        }
    }

    public boolean isTransaction() {
        return connection != null && transaction && !autoCommit;
    }

    public void rollback() {
        if (isTransaction()) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new JdbcException(e);
            } finally {
                transaction = false;
            }
        } else {
            throw new JdbcException("cannot rollback - transaction not started");
        }
    }

    public void commit() {
        if (isTransaction()) {
            try {
                connection.commit();
            } catch (SQLException e) {
                throw new JdbcException(e);
            } finally {
                transaction = false;
            }
        } else {
            throw new JdbcException("cannot commit - transaction not started");
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getConnectionString() {
        return properties.getProperty(JdbcConfig.JDBC_URL);
    }

    public void close() {
        if (connection != null) {
            Connection tc = connection;
            connection = null;
            try {
                tc.close();
            } catch (SQLException e) {
                throw new JdbcException(e);
            }
        }
    }

    public void init(String driver, String url, Properties props) {
        if (driver != null) properties.setProperty(JdbcConfig.JDBC_DRIVER, driver);
        if (url != null) properties.setProperty(JdbcConfig.JDBC_URL, url);
        properties.putAll(props);
    }
}
