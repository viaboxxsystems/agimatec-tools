package com.agimatec.utility.fileimport;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Description: a row in table import_control can be represented by an instance
 * of this class. <br/>
 * User: roman.stumm <br/>
 * Date: 30.08.2007 <br/>
 * Time: 13:29:51 <br/>
 * Copyright: Agimatec GmbH
 */
public class ImportControl implements Serializable {
   protected long importId;
   protected String importName;
   protected Timestamp startTime;
   protected Timestamp endTime;
   protected String status;
   protected Integer rowCount;
   protected Integer errorCount;

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

    public String getStatus() {
        return status;
    }


}
