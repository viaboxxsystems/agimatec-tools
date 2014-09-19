package com.agimatec.sql.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Description:</b>   Hold information about a foreign key in the database<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 */
@XStreamAlias("foreignKey")
public class ForeignKeyDescription extends A_IntegrityRuleDescription {
    private String comment;
    private String constraintName;   // the name of the foreign-key constraint, not the index-name
    private String refTableName;     // the name of the referenced table
    private List<String> columns =
        new ArrayList(); // list of String , foreign key columns
    private List<String> refColumns =
        new ArrayList(); // list of String , foreign key referenced columns
    private String onDeleteRule;    // e.g. "CASCADE", null/"RESTRICT", "SET NULL"

    public ForeignKeyDescription deepCopy() {
        try {
            ForeignKeyDescription clone = (ForeignKeyDescription) clone();
            clone.columns = new ArrayList(columns);
            clone.refColumns = new ArrayList(refColumns);
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * do NOT compare the constraintName, but the columns, refColumns, tables (independent of sequence)
     *
     * @param other
     * @return true when similar, false otherwise
     */
    public boolean isSimilarTo(ForeignKeyDescription other) {
        if (getColumnSize() == other.getColumnSize() &&
            getRefTableName().equalsIgnoreCase(other.getRefTableName()) &&
            getRefColumns().size() == other.getRefColumns().size()) {
            for (String column : getColumns()) {
                if (other.getColumn(column) == -1) return false;
            }
            for (String column : getRefColumns()) {
                if (other.getRefColumn(column) == -1) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @param aTable - the tableDescription of My Table
     * @return true when all of my columns are nullable
     */
    public boolean isNullable(TableDescription aTable) {
        if (!aTable.getTableName().equalsIgnoreCase(getTableName()))
            throw new IllegalArgumentException(
                "Illegal table " + aTable + " for " + this);
        for (int i = 0; i < columns.size(); i++) {
            String col = getColumn(i);
            if (!aTable.getColumn(col).isNullable()) return false;
        }
        return true;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String aConstraintName) {
        constraintName = aConstraintName;
    }

    public String getRefTableName() {
        return refTableName;
    }

    public void setRefTableName(String aRefTableName) {
        refTableName = aRefTableName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String aComment) {
        comment = aComment;
    }

    public int getColumn(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            if (col.equalsIgnoreCase(columnName)) return i;
        }
        return -1;
    }

    public int getRefColumn(String columnName) {
        for (int i = 0; i < refColumns.size(); i++) {
            String col = refColumns.get(i);
            if (col.equalsIgnoreCase(columnName)) return i;
        }
        return -1;
    }

    public String getColumn(int i) {
        return columns.get(i);
    }

    public String getRefColumn(int i) {
        return refColumns.get(i);
    }

    public int getColumnSize() {
        return columns.size();
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<String> getRefColumns() {
        return refColumns;
    }

    public void addColumnPair(String column, String refColumn) {
        columns.add(column);
        refColumns.add(refColumn);
    }

    public String toString() {
        return getConstraintName();
    }

    public boolean containsColumn(String columnName) {
        for (String each : columns) {
            if (each.equalsIgnoreCase(columnName)) return true;
        }
        return false;
    }

    public void setOnDeleteRule(String string) {
        onDeleteRule = string;
    }

    public String getOnDeleteRule() {
        return onDeleteRule;
    }
}
