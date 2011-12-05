package de.viaboxx.dbmigrate.spring;

import com.agimatec.dbmigrate.AutoMigrationTool;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 04.11.2011<br>
 * Time: 09:51:39<br>
 * viaboxx GmbH, 2011
 */
class DBCPAdapter {
    private BasicDataSource dataSource;

    /**
     * you can optionally configure the database connection for dbmigrate by
     * providing a BasicDataSource. Otherwise dbmigrate uses its proprietary database connection settings.
     * (for proprietary configuration, see properties file configured with<pre>
     * &lt;file name="JdbcConfig" file="dbconnect.properties"/&gt;</pre>
     * or provide these information in the xml configuration,
     * read more here:
     * <a href="http://code.google.com/p/agimatec-tools/wiki/DBMigrateConfigFile">DBMigrateConfigFile</a>
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = (BasicDataSource)dataSource;
    }

    public void configure(AutoMigrationTool tool) {
        if (dataSource != null) { // configure from dataSource
            if (StringUtils.isNotEmpty(dataSource.getUsername()) && tool.getEnvironment().get("DB_USER") == null) {
                tool.getEnvironment().put("DB_USER", dataSource.getUsername());
            }
            if (StringUtils.isNotEmpty(dataSource.getPassword()) && tool.getEnvironment().get("DB_PASSWORD") == null) {
                tool.getEnvironment().put("DB_PASSWORD", dataSource.getPassword());
            }
            if (StringUtils.isNotEmpty(dataSource.getUrl()) && tool.getEnvironment().get("DB_URL") == null) {
                tool.getEnvironment().put("DB_URL", dataSource.getUrl());
            }
            if (StringUtils.isNotEmpty(dataSource.getDriverClassName()) &&
                    tool.getEnvironment().get("DB_DRIVER") == null) {
                tool.getEnvironment().put("DB_DRIVER", dataSource.getDriverClassName());
            }
        }
    }
}
