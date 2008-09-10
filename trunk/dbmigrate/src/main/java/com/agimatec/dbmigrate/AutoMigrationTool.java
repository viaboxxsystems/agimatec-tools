package com.agimatec.dbmigrate;

import com.agimatec.commons.config.Config;
import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.config.FileNode;
import com.agimatec.dbmigrate.util.ChangeDirCommand;
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
    private boolean isSim;

    public AutoMigrationTool() {
        super();
    }

    public static void main(String[] args) throws Exception {
        AutoMigrationTool tool = new AutoMigrationTool();
        if (!tool.parseArgs(args)) return;
        try {
            tool.setUp();
            tool.startAutomaticMigration();
        } finally {
            tool.tearDown();
        }
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
            }
        }
        isSim = "true".equalsIgnoreCase(sim) || "yes".equalsIgnoreCase(sim);
        return true;
    }

    private void printUsage() {
        System.out.println("usage: java " + getClass().getName() +
                " -sim false -conf migration.xml");
        System.out.println("Options:\n\t-help \t (optional) print this help");
        System.out.println(
                "\t-sim \t (optional) true|yes=simulation only, default is false");
        System.out.println(
                "\t-conf \t (optional) name of migration.xml configuration file, default is migration.xml");
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
        log("------------------------------------");
        prepareDatabase();
        List versionFiles = createFileList();

        if (isSim) log("SIMULATION ONLY - SEQUENCE FOLLOWS:");
        if (versionFiles.isEmpty()) {
            log("THERE ARE NO NEWER FILES TO EXECUTE FOR THIS VERSION.");
            return;
        }
        int i = 1;
        try {
            for (Object any : versionFiles) {
                if (any instanceof ChangeDirCommand) {
                    log("changing script directory to " +
                            ((ChangeDirCommand) any).getDir());
                    setScriptsDir(((ChangeDirCommand) any).getDir());
                } else if (any instanceof DBVersionString) {
                    DBVersionString each = (DBVersionString) any;
                    if ("xml".equalsIgnoreCase(each.getFileType())) {
                        String filePath = getScriptsDir() + each.getFileName();
                        log("STEP " + (i++) + " (of " + versionFiles.size() +
                                ") = XML Config: " + filePath);
                        Config cfg = ConfigManager.getDefault()
                                .readConfig(filePath, false);
                        if (!isSim) {
                            try {
                                prepareLocalEnvironment(cfg);
                                perform(cfg.getList("Operations"));
                            } finally {
                                localEnv =
                                        null;    // remove localEnv after exec. of config
                            }
                        }
                    } else if ("sql".equalsIgnoreCase(each.getFileType())) {
                        log("STEP " + (i++) + " (of " + versionFiles.size() +
                                ") = SQL Script: " + each.getFileName());
                        if (!isSim) {
                            doSQLScript(each.getFileName());
                        }
                    } else if ("groovy".equalsIgnoreCase(each.getFileType())) {
                        log("STEP " + (i++) + " (of " + versionFiles.size() +
                                ") = Groovy Script: " + each.getFileName());
                        if (!isSim) {
                            doGroovyScript(each.getFileName());
                        }
                    } else {
                        log("not a supported file type: " + each.getFileName());
                    }
                }
            }
        } catch (Exception ex) {
            rollback();
            log(ex);
            throw ex;
        }
    }


    private void prepareLocalEnvironment(Config cfg) {
        Map tempEnv = cfg.getMap("env");
        if (tempEnv != null) { // merge env
            localEnv = new HashMap(getMigrateEnvironment());
            localEnv.putAll(tempEnv);
        }
    }

    private DBVersionString getToVersion() {
        String ver = getMigrateConfig().getString("to-version");
        if (ver == null || ver.length() == 0) {
            return null;
        } else {
            log("Using to-version: " + ver);
            return DBVersionString.fromString(ver);
        }
    }

    private DBVersionString getFromVersion() throws SQLException {
        String ver = getMigrateConfig().getString("from-version");
        DBVersionString version;
        if (ver == null || ver.length() == 0) {
            version = readVersion();
            log("Current database version: " + version);
        } else {
            version = DBVersionString.fromString(ver);
            log("Using from-version: " + version);
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

    private List createFileList()
            throws SQLException, MalformedURLException, URISyntaxException {
        List files = filterVersions(getFromVersion(), readDir("up-", getScriptsDir()));
        String dir = getBeforeAllScriptsDir();
        if (dir != null) {
            List before = readDir(null, dir);
            before.add(0, new ChangeDirCommand(dir));
            before.add(new ChangeDirCommand(getScriptsDir()));
            before.addAll(files);
            files = before;
        }
        dir = getAfterAllScriptsDir();
        if (dir != null) {
            files.add(new ChangeDirCommand(dir));
            files.addAll(readDir(null, dir));
        }
        return files;
    }

    /**
     * read possible scripts and configs
     *
     * @return them in a sorted order, sorted by execution sequence
     */
    private List<DBVersionString> readDir(String prefix, String directory)
            throws MalformedURLException, URISyntaxException {
        if(directory == null) return new ArrayList();
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
