package com.agimatec.sql.meta.postgres;

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
 * Description: <br>
 * <p>
 * Date: 19.09.14<br>
 * </p>
 */
public class PostgresSchemaCheckerTest {
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
    public void validatePostgresQuartzTables() throws Exception {
        JdbcConfig config = new JdbcConfig();
        config.setDriver("org.postgresql.Driver");
        config.setConnect("jdbc:postgresql://localhost:5432/dhlnl_server");
        config.getProperties().put("user", "di");
        config.getProperties().put("password", "7n9aMHKh");
        targetDatabase = JdbcDatabaseFactory.createInstance(config);
        targetDatabase.begin();

        DatabaseSchemaChecker checker = DatabaseSchemaChecker.forDbms("postgres");
        checker.setDatabase(targetDatabase);
        List<URL> urls = new ArrayList<URL>();
        urls.add(getClass().getClassLoader().getResource("postgres/quartz-0.4.2-postgres.sql"));
        checker.checkDatabaseSchema(urls.toArray(new URL[urls.size()]));

    }


}
