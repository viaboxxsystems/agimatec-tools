package com.agimatec.dbhistory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 18:21:29 <br/>
 */
@XStreamAlias("tableConfig")
public class HistTableConfig implements Serializable {
    private Set<String> excludeColumns;
    private final String tableName;
    private String historyTable;
    private String insertTrigger;
    private String updateTrigger;
//    private boolean writeObsoleteUpdate; // ggf. spaeter mal...

    public HistTableConfig(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getHistoryTable() {
        if(historyTable == null) return "H_" + tableName;
        return historyTable;
    }

    /**
     * (Optional)
     * The name of the history table for this table.
     * Default is H_${@name}. If this text-node is missing or if the value is empty,
     * no history table will be generated.
     *
     * @param historyTable
     */
    public void setHistoryTable(String historyTable) {
        this.historyTable = historyTable;
    }

    public String getInsertTrigger() {
        if(insertTrigger == null) return "TR_I_" + tableName;
        return insertTrigger;
    }

    /**
     * (Optional)
     * If an empty @value is entered or if the @value attribute is missing,
     * NO INSERT Trigger will be generated.
     * If this text-node is missing, a default name will be generated (TR_I_${@name})
     * The @value attribute specifies an explicit trigger name.
     *
     * @param insertTrigger
     */
    public void setInsertTrigger(String insertTrigger) {
        this.insertTrigger = insertTrigger;
    }

    public String getUpdateTrigger() {
        if(updateTrigger == null) return "TR_U_" + tableName;
        return updateTrigger;
    }

    /**
     * (Optional)
     * If an empty @value is entered or if the @value attribute is missing,
     * NO UPDATE Trigger will be generated.
     * If this text-node is missing, a default name will be generated (TR_H_${@name})
     * The @value attribute specifies an explicit trigger name.
     *
     * @param updateTrigger
     */
    public void setUpdateTrigger(String updateTrigger) {
        this.updateTrigger = updateTrigger;
    }

    /**
     * (Optional)
     * A List of column names to exclude columns from the history table.
     * Default is that all columns are part of the history table (e.g. none excluded).
     * Note 2: You MUST NOT exclude primary key and versioning columns
     * because they are PrimayKey of the HistoryTable.
     *
     * @return
     */
    public Set<String> getExcludeColumns() {
        if (excludeColumns == null) excludeColumns = new HashSet();
        return excludeColumns;
    }
}
