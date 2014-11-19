package com.agimatec.sql.meta;

import java.io.Serializable;

/**
 * <b>Description:</b>   Base class for different kinds of integrity rule descriptions<br>
 * <b>Creation Date:</b> 17.11.2007
 *
 * @author Roman Stumm
 */
public abstract class A_IntegrityRuleDescription implements Serializable, Cloneable {
    protected String tableName;
    protected String tableSpace;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String aTableName) {
        tableName = aTableName;
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public void setTableSpace(String aTableSpace) {
        tableSpace = aTableSpace;
    }
    public abstract int getColumnSize();

    public abstract String getColumn(int i);
}
