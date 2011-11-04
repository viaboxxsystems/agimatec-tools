package de.viaboxx.dbmigrate.spring;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.dbmigrate.AutoMigrationTool;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Description:
 * <strong>Bean to integrate and configure dbmigrate via spring.</strong>
 * <br>
 * <br>
 * <p/>
 * Supports spring access common options of AutoMigrationTool: <br>
 * - sim: simulation (false)<br>
 * - migrateConfigFileName: xml setup file (migration.xml)<br>
 * <p/>
 * <br>Currently not supported features via spring-configuration: <br>
 * - script: ScriptAction   <br>
 * - op:     OperationAction<br>
 * <br>
 * User: roman.stumm<br>
 * Date: 03.11.2011<br>
 * Time: 08:06:42<br>
 * viaboxx GmbH, 2011
 */
public class DBMigrateBean implements InitializingBean, BeanNameAware {
    private final AutoMigrationTool tool = new AutoMigrationTool();

    private String beanName;

    private DBCPAdapter dataSourceConfigurator;

    /**
     * disable running the tool
     */
    private boolean disabled = false;

    /**
     * @param fileName - default is "migration.xml", see BaseMigrationTool#migrateConfigFileName
     */
    public void setMigrateConfigFileName(String fileName) {
        tool.setMigrateConfigFileName(fileName);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @param sim - default is 'false'
     */
    public void setSimulation(boolean sim) {
        tool.setSim(sim);
    }

    public void setConfigRootUrl(String configRoot) {
        ConfigManager.getDefault().setConfigRootPath(configRoot);
    }

    @SuppressWarnings({"unchecked"})
    public void setToVersion(String version) {
        tool.getEnvironment().put("to-version", version);
    }

    public void setEnvironment(Map<String, Object> env) {
        for (Map.Entry<String, Object> entry : env.entrySet()) {
            if (null == System.getProperty(entry.getKey())) { // do not overwrite System properties
                //noinspection unchecked
                tool.getEnvironment().put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setMigrateConfig(Map<Object, Object> conf) {
        for (Map.Entry entry : conf.entrySet()) {
            tool.getMigrateConfig().put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return the underlying dbmigrate tool instance (never null)
     */
    public AutoMigrationTool getTool() {
        return tool;
    }

    /**
     * Automatically start dbmigrate. Throw exception when something goes wrong.
     *
     * @throws Exception - all exceptions when dbmigrate not successful
     */
    public void afterPropertiesSet() throws Exception {
        if (isDisabled()) {
            tool.getLog().info(beanName + " - execution disabled!");
            return; // do nothing
        }
        try {
            tool.getLog().info(beanName + " - initializing");
            configure();
            tool.setUp();
            tool.startAutomaticMigration();
            tool.getLog().info(beanName + " - run successfully");
        } catch (Exception ex) {
            tool.getLog().error(beanName + " - run with exception", ex);
        } finally {
            tool.tearDown();
            tool.getLog().info(beanName + " - terminated");
        }
    }

    @SuppressWarnings({"unchecked"})
    private void configure() {
        if (dataSourceConfigurator != null) dataSourceConfigurator.configure(tool);
        if (tool.getMigrateConfig().get("Scripts-Prefix") == null) {
            tool.getMigrateConfig()
                    .put("Scripts-Prefix", "up_"); // change default so that groovy scripts have compatible names
        }
    }

    public void setBeanName(String name) {
        beanName = name;
    }

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
        if (dataSource != null) {
            // not yet implemented: support other dataSource types than apache.commons.dbcp
            // decouple this class from compile-dependencies to apache.commons.dbcp or other implementations
            dataSourceConfigurator = new DBCPAdapter();
            dataSourceConfigurator.setDataSource(dataSource);
        }
    }
}
