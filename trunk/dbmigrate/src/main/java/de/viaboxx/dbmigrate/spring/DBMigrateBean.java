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

    protected String beanName;

    protected DBCPAdapter dataSourceConfigurator;

    /**
     * disable running the tool
     */
    private boolean disabled = false;
    private boolean stopOnException = true;

    /**
     * @param fileName - default is "migration.xml", see BaseMigrationTool#migrateConfigFileName
     */
    public void setConfigFile(String fileName) {
        getTool().setMigrateConfigFileName(fileName);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isStopOnException() {
        return stopOnException;
    }

    public void setStopOnException(boolean stopOnException) {
        this.stopOnException = stopOnException;
    }

    /**
     * @param sim - default is 'false'
     */
    public void setSimulation(boolean sim) {
        getTool().setSim(sim);
    }

    public void setConfigRootUrl(String configRoot) {
        ConfigManager.getDefault().setConfigRootPath(configRoot);
    }

    @SuppressWarnings({"unchecked"})
    public void setToVersion(String version) {
        getTool().getEnvironment().put("to-version", version);
    }

    public void setEnvironment(Map<String, Object> env) {
        for (Map.Entry<String, Object> entry : env.entrySet()) {
            if (null == System.getProperty(entry.getKey())) { // do not overwrite System properties
                //noinspection unchecked
                getTool().getEnvironment().put(entry.getKey(), entry.getValue());
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
            getTool().getLog().info(beanName + " - execution disabled!");
            return; // do nothing
        }
        try {
            getTool().getLog().info(beanName + " - initializing");
            configure();
            getTool().setUp();
            getTool().startAutomaticMigration();
            getTool().getLog().info(beanName + " - successful");
        } catch (Exception ex) {
            getTool().getLog().error(beanName + " - failed", ex);
            if (isStopOnException()) throw ex; // propagate to stop spring from starting
        } finally {
            getTool().tearDown();
            getTool().getLog().info(beanName + " - finished");
        }
    }

    @SuppressWarnings({"unchecked"})
    private void configure() {
        if (dataSourceConfigurator != null) dataSourceConfigurator.configure(getTool());
        if (getTool().getMigrateConfig().get("Scripts-Prefix") == null) {
            getTool().getMigrateConfig()
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
