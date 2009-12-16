package com.agimatec.dbmigrate;

import com.agimatec.commons.config.Config;
import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.config.FileNode;
import com.agimatec.dbmigrate.action.ChangeDirCommand;
import com.agimatec.dbmigrate.action.MigrateAction;
import com.agimatec.dbmigrate.action.OperationAction;
import com.agimatec.dbmigrate.action.ScriptAction;
import com.agimatec.dbmigrate.util.SQLCursor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

/**
 * Automatical database migration program for Agimatec GmbH
 * <pre>
 * Features:
 * 01- detect from-version from database or config-file
 * 02- execute sql-scripts or commands from a config file for
 *     all files that belong to versions later than "from-version".
 * 03- automatic script detection and sorting.
 * 04- can stop execution at "to-version", if set in config-file.
 * 05- supports conditional execution in sql-scripts (-- #if #endif syntax)
 * 06- support conditional execution in config-files (list-tags)
 * 07- supports enviroment variables in sql-scripts ( ${variable} syntax )
 * 08- supports connect commands in SQL-scripts
 * 09- a) supports local-enviroment variables per config-file of each migration version,
 *     b) environment variables per migration config,
 *     c) all JVM System-Properties are accessible as environment variable default
 * 10- supports Simulation-mode "with jvm parameter -Dsim=true" to check
 *     behavior before affecting the system.
 * 11- supports subscripts (@scriptname.sql; syntax)
 * 12 - supports db_version upgrade with setVersion(dbversion)-method
 *      or -- @version(dbversion) script-directive
 * </pre>
 * Author: Roman Stumm
 * Date: 04.04.2007
 * <pre>
 * final String sim = System.getProperty(SYSTEM_PROPERTY_SIM);
 * sim = "true"|"yes" :: simlation, echo execution sequence into log, but do not invoke any script
 * otherwise (=default) :: execute scripts/java in sequence
 * </pre>
 */
public class AutoMigrationTool extends BaseMigrationTool {
    // the local environment entries of a automatically found .xml config
    private Map localEnv = null;
    private boolean sim;
    private List<MigrateAction> actionOverride;

    public AutoMigrationTool() {
        super();
    }

    /**
     * run the tool and exit the JVM afterwards.
     *
     * @throws Exception
     * exit(0) = successful
     * exit(1) = in case of an exception
     */
    public static void main(String[] args) {
        AutoMigrationTool tool = new AutoMigrationTool();
        try {
            if (!tool.parseArgs(args)) return;
            try {
                tool.setUp();
                tool.startAutomaticMigration();
            } finally {
                tool.tearDown();
            }
            System.exit(0);
        } catch (Throwable ex) {
            log.fatal(null, ex);
            System.exit(1);
        }
    }

    public boolean isSim() {
        return sim;
    }

    private boolean parseArgs(String[] args) {

        String sim = "false";
        for (int i = 0; i < args.length; i++) {
            String each = args[i];
            if ("-sim".equalsIgnoreCase(each)) {
                i++;    // skip next param
                sim = args[i];
            } else if ("-conf".equalsIgnoreCase(each)) {
                i++;    // skip next param
                setMigrateConfigFileName(args[i]);
            } else if ("-help".equalsIgnoreCase(each)) {
                printUsage();
                return false;
            } else if ("-script".equalsIgnoreCase(each)) {
                i++;
                ScriptAction action = ScriptAction.create(this, args[i]);
                if(action != null) {
                    addActionOverride(action);
                }
            } else if ("-op".equalsIgnoreCase(each)) {
                String op = args[++i];
                String param = "";
                i++;
                if (i < args.length) {
                    param = args[i];
                }
                addActionOverride(new OperationAction(this, op, param));
            }
        }
        this.sim = "true".equalsIgnoreCase(sim) || "yes".equalsIgnoreCase(sim);
        return true;
    }

    private void addActionOverride(MigrateAction operationAction) {
        if (actionOverride == null) actionOverride = new LinkedList();
        actionOverride.add(operationAction);
    }

    private void printUsage() {
        System.out.println("usage: java " + getClass().getName() +
                " -sim false -conf migration.xml -script aScript -op operationName operationParameter ");
        System.out.println("Options:\n\t-help \t (optional) print this help");
        System.out.println(
                "\t-sim \t (optional) true|yes=simulation only, default is false");
        System.out.println(
                "\t-conf \t (optional) name of migration.xml configuration file, default is migration.xml");
        System.out.println(
                "\t-script \t (optional, multiple occurrence supported) name of a upgrade-file (sql, groovy, xml) with operations. tool will execute the given file(s) only!");
        System.out.println(
                "\t-op \t (optional, multiple occurrence supported) the operation in the same syntax as in an upgrade-file. tool will execute the given operation(s) only!");
    }

    // overwritten to provide the enviroment (or local env) to the script executor
    public Map getEnvironment() {
        if (localEnv == null) {
            return super.getEnvironment();
        } else {
            return localEnv;
        }
    }

    protected Map getMigrateEnvironment() {
        return super.getEnvironment();
    }

    public void startAutomaticMigration() throws Exception {
        log("----------------- start migration -----------------");
        prepareDatabase();
        if (actionOverride != null && !actionOverride.isEmpty()) {
            print("PERFORMING COMMAND LINE ACTIONS ONLY!");
            performActions(actionOverride);
        } else {
            performActions(createActions());
        }
    }

    private void performActions(List<MigrateAction> actionOverride) throws Exception {
        if (sim) print("SIMULATION ONLY - SEQUENCE FOLLOWS:");
        try {
            if (actionOverride.isEmpty()) {
                print("THERE ARE NO ACTIONS TO PERFORM.");
            } else {
                int i = 0;
                for (MigrateAction each : actionOverride) {
                    i++;
                    print("ACTION " + i + " (of " + actionOverride.size() + ") = " +
                            each.getInfo());
                    each.doIt();
                }
            }
        } catch (Exception ex) {
            rollback();
            log(ex);
            throw ex;
        }
    }

    protected void prepareLocalEnvironment(Config cfg) {
        Map tempEnv = cfg.getMap("env");
        if (tempEnv != null) { // merge env
            localEnv = new HashMap(getMigrateEnvironment());
            localEnv.putAll(tempEnv);
            replaceProperties(localEnv);
        }
    }

    public void doXmlScript(String filePath) throws Exception {
        if (!sim) {
            Config cfg = ConfigManager.getDefault()
                    .readConfig(filePath, false);
            try {
                prepareLocalEnvironment(cfg);
                perform(cfg.getList("Operations"));
            } finally {
                localEnv =
                        null;    // remove localEnv after exec. of config
            }
        }
    }

    private DBVersionString getToVersion() {
        String ver = getMigrateConfig().getString("to-version");
        if (ver == null || ver.length() == 0) {
            return null;
        } else {
            print("Using to-version: " + ver);
            return DBVersionString.fromString(ver);
        }
    }

    private DBVersionString getFromVersion() throws SQLException {
        String ver = getMigrateConfig().getString("from-version");
        DBVersionString version;
        if (ver == null || ver.length() == 0) {
            version = readVersion();
            print("Current database version: " + version);
        } else {
            version = DBVersionString.fromString(ver);
            print("Using from-version: " + version);
        }
        return version;
    }

    /**
     * remove all entries from the list that are not relevant
     * for direct execution from the given dbVersion
     *
     * @param version - the current database version
     */
    private List<DBVersionString> filterVersions(DBVersionString version,
                                                 List<DBVersionString> versionFiles) {
        Iterator<DBVersionString> iter = versionFiles.iterator();
        DBVersionString toversion = getToVersion();
        while (iter.hasNext()) {
            DBVersionString each = iter.next();
            if (!each.isLater(version)) {
                iter.remove();
            } else if (toversion != null && each.isLater(toversion)) {
                iter.remove();
            }
        }
        return versionFiles;
    }

    /**
     * read the version from the database
     *
     * @throws SQLException
     */
    private DBVersionString readVersion() throws SQLException {
        String version = null;
        try {
            SQLCursor rs = sqlSelect(dbVersionMeta.toSQLSelectVersion());
            try {
                while (rs.next()) {
                    version = rs.getString(1);
                }
            } finally {
                rs.close();
            }
        } catch (SQLException ex) { // we assume: no table DB_VERSION in database
            log.warn("cannot read " + dbVersionMeta.getQualifiedVersionColumn(), ex);
        }
        return version == null ? null : DBVersionString.fromString(version);
    }

    private List<MigrateAction> createActions()
            throws SQLException, MalformedURLException, URISyntaxException {
        List<DBVersionString> files =
                filterVersions(getFromVersion(), readDir("up-", getScriptsDir()));
        List<MigrateAction> actions;
        String dir = getBeforeAllScriptsDir();
        if (dir != null) {
            List<DBVersionString> before = readDir(null, dir);
            actions = createActions(before);
            actions.add(0, new ChangeDirCommand(this, dir));
            actions.add(new ChangeDirCommand(this, getScriptsDir()));
            actions.addAll(createActions(files));
        } else {
            actions = createActions(files);
        }
        dir = getAfterAllScriptsDir();
        if (dir != null) {
            actions.add(new ChangeDirCommand(this, dir));
            actions.addAll(createActions(readDir(null, dir)));
        }
        return actions;
    }

    private List<MigrateAction> createActions(List<DBVersionString> files) {
        List<MigrateAction> actions = new LinkedList();
        for (DBVersionString file : files) {
            ScriptAction action =
                  ScriptAction.create(this, file.getFileName(), file.getFileType());
            if(action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    /**
     * read possible scripts and configs
     *
     * @return them in a sorted order, sorted by execution sequence
     */
    private List<DBVersionString> readDir(String prefix, String directory)
            throws MalformedURLException, URISyntaxException {
        if (directory == null) return new ArrayList();
        File scriptsDir = new File(directory);
        File[] files = scriptsDir.listFiles();
        List<DBVersionString> order = new ArrayList<DBVersionString>(files.length);
        for (File each : files) {
            if (each.isFile()) {
                DBVersionString ver = DBVersionString.fromString(prefix, each.getName());
                if (ver != null) order.add(ver);
            }
        }
        Collections.sort(order);
        return order;
    }

    protected String getBeforeAllScriptsDir() {
        FileNode dir = (FileNode) getMigrateConfig().get("Scripts-Before-All");
        return (dir == null) ? null : dir.getFilePath();
    }

    protected String getAfterAllScriptsDir() {
        FileNode dir = (FileNode) getMigrateConfig().get("Scripts-After-All");
        return (dir == null) ? null : dir.getFilePath();
    }

}
