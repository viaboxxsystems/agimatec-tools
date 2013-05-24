package com.agimatec.sql.meta.mysql;

import com.agimatec.jdbc.JdbcConfig;
import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.jdbc.JdbcDatabaseFactory;
import com.agimatec.sql.meta.checking.DatabaseSchemaChecker;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p></p>
 * User: roman.stumm@viaboxx.de<br>
 * Date: 26.03.13
 */
public class MySqlSchemaCheckerTest {
    private JdbcDatabase targetDatabase;
    @After
    public void tearDown() {
        if (targetDatabase != null && targetDatabase.getConnection() != null) {
            targetDatabase.rollback();
            targetDatabase.close();
        }
    }

    @Test
    @Ignore
    public void validateMySqlSchema() throws Exception {
        JdbcConfig config = new JdbcConfig();
        config.setDriver("com.mysql.jdbc.Driver");
        config.setConnect("jdbc:mysql://localhost:3306/mysql_db");
        config.getProperties().put("user", "root");
        config.getProperties().put("password", "");
        targetDatabase = JdbcDatabaseFactory.createInstance(config);
        targetDatabase.begin();

        DatabaseSchemaChecker checker = DatabaseSchemaChecker.forDbms("mysql");
        checker.setDatabase(targetDatabase);
        List<URL> urls = new ArrayList<URL>();
        urls.add(getClass().getClassLoader().getResource("mysql/mysql-schema.sql"));
        checker.checkDatabaseSchema(urls.toArray(new URL[urls.size()]));

    }

}
