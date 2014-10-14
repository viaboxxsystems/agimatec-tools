package com.agimatec.utility.fileimport;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Description: a row in table import_control can be represented by an instance
 * of this class. <br/>
 * User: roman.stumm <br/>
 * Date: 30.08.2007 <br/>
 * Time: 13:29:51 <br/>
 */
public class ImportControl implements Serializable {
   protected long importId;
   protected String importName; // Unique. mandatory. import_type
   protected Timestamp startTime;
   protected Timestamp endTime;
   protected ImportState status;
   protected Integer rowCount;
   protected Integer errorCount;
   protected String description;  // optional. description of the import. provided by user.
   protected String fileName; // optional. source file name.
   protected String errorMessage; // optional. provided by server on failure.

    public Timestamp getEndTime() {
        return endTime;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public long getImportId() {
        return importId;
    }

    public String getImportName() {
        return importName;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public void setImportId(long importId) {
        this.importId = importId;
    }

    public void setImportName(String importName) {
        this.importName = importName;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public ImportState getStatus() {
        return status;
    }

    public void setStatus(ImportState status) {
        this.status = status;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
