package com.agimatec.database;

import com.agimatec.dbmigrate.MigrationTool;
import com.agimatec.dbmigrate.MigrationToolAware;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 13:56:19 <br/>
 */
public abstract class AbstractDbTool implements MigrationToolAware {
    protected Connection jdbcConnection;
    protected IDatabaseConnection connection;
    protected String dataFile = "data.xml";

    public Connection getJdbcConnection() {
        return jdbcConnection;
    }

    public void setJdbcConnection(Connection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    public IDatabaseConnection getConnection() {
        return connection;
    }

    public void setConnection(IDatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * invoked by {@link com.agimatec.dbmigrate.BaseMigrationTool}
     */
    public void setMigrationTool(MigrationTool tool) {
        jdbcConnection = tool.getTargetDatabase().getConnection();
    }

    protected void disconnect() throws Exception {
        if(jdbcConnection != null) jdbcConnection.commit();
        if (connection != null) {            
            connection.getConnection().commit();
            connection.close();
            connection = null;
        }
    }
    
    protected void connectJdbc(String[] args) throws SQLException,
            ClassNotFoundException {
        connectJdbc(args[0], args[1], args[2], args[3]);
    }

    protected void connectJdbc(String driver, String url, String user, String password)
            throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        jdbcConnection = DriverManager.getConnection(url, user, password);
    }

    protected void connectDbUnit() throws SQLException, ClassNotFoundException, DatabaseUnitException {
        if(connection == null)
            connection = new DatabaseConnection(jdbcConnection);
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String insertDataFile) {
        this.dataFile = insertDataFile;
    }
}
