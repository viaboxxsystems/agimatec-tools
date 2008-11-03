package com.agimatec.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * INSERT dataset into a database.
 * Optional execution of DELETE with a different dataset possible.
 */
public class DbUnitSetupTool extends AbstractDbTool {
    protected static final Log log = LogFactory.getLog(DbUnitSetupTool.class);

    private String deleteDataFile = "delete_data.xml";
    /**
     * <pre>
     * REFRESH: data of existing rows are updated and non-existing row get inserted.
     * Any rows which exist in the database but not in dataset stay unaffected.
     * <p/>
     * INSERT: 	(Default) This operation inserts the dataset contents into the database.
     * This operation assumes that table data does not exist in the target database and fails if this is not the case.
     * To prevent problems with foreign keys, tables must be sequenced appropriately in the dataset.
     * <p/>
     * UPDATE: This operation updates the database from the dataset contents.
     * This operation assumes that table data already exists in the target database and
     * fails if this is not the case.
     * </pre>
     */
    private String operation = "INSERT";

    public String getDeleteDataFile() {
        return deleteDataFile;
    }

    public void setDeleteDataFile(String deleteDataFile) {
        this.deleteDataFile = deleteDataFile;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void execute() throws Exception {
        connectDbUnit();

        log.info("populating database from " + dataFile + " after deletion with " +
                deleteDataFile);

        if (deleteDataFile != null && deleteDataFile.length() > 0) {
            IDataSet deleteAllDataSet = DataSetFactory.createDataSet(deleteDataFile).load();
            DatabaseOperation.DELETE_ALL.execute(connection, deleteAllDataSet);
            // noch mal, wegen history tables
            DatabaseOperation.DELETE_ALL.execute(connection, deleteAllDataSet);
        }
        if (dataFile != null && dataFile.length() > 0) {
            IDataSet testDataSet = DataSetFactory.createDataSet(dataFile).load();
            if ("INSERT".equals(operation)) {
                DatabaseOperation.INSERT.execute(connection, testDataSet);
            } else if ("REFRESH".equals(operation)) {
                DatabaseOperation.REFRESH.execute(connection, testDataSet);
            } else if ("UPDATE".equals(operation)) {
                DatabaseOperation.UPDATE.execute(connection, testDataSet);
            } else {
                throw new UnsupportedOperationException(
                        "DatabaseOperation." + operation + " not supported.");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        DbUnitSetupTool setUp = new DbUnitSetupTool();
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
                if ("-i".equals(each)) {
                    i++;
                    setDataFile(args[i]);
                } else if ("-d".equals(each)) {
                    i++;
                    setDeleteDataFile(args[i]);
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
                " {driver} {url} {user} {password} [-d {deleteDataFile}] [-i {insertDataFile}]");
    }
}
