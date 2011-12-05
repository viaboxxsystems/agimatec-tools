package com.agimatec.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.DatabaseUnitException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 13:56:07 <br/>
 * Copyright: Agimatec GmbH
 */
public class DbUnitDumpTool extends AbstractDbTool {
    protected static final Log log = LogFactory.getLog(DbUnitDumpTool.class);

    public void execute() throws SQLException, DatabaseUnitException, IOException,
            ClassNotFoundException {
        connectDbUnit();
        File file = new File(dataFile);
        System.out.println("Writing file " + file.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(file);
        try {
            DataSetFactory.createDataSet(dataFile)
                    .write(connection.createDataSet(), out);
        } finally {
            out.close();
        }

    }

    public static void main(String[] args) throws Exception {
        DbUnitDumpTool setUp = new DbUnitDumpTool();
        if (!setUp.runMain(args)) System.exit(-1);
    }

    protected boolean runMain(String[] args) throws Exception {
        try {
            if (!parseArgs(args)) return false;
            execute();
        } finally {
            disconnect();
        }
        return true;
    }

    protected boolean parseArgs(String[] args) {
        try {
            connectJdbc(args);
            for (int i = 4; i < args.length; i++) {
                String each = args[i];
                if ("-f".equals(each)) {
                    i++;
                    setDataFile(args[i]);
                }
            }
            return true;
        } catch (Exception ex) {
            printUsage();
            log.error("invalid parameters", ex);
            return false;
        }
    }

    protected void printUsage() {
        System.out.println("usage:\njava " + getClass().getName() +
                " {driver} {url} {user} {password} [-f {outputDataFile}] ");
    }
}
