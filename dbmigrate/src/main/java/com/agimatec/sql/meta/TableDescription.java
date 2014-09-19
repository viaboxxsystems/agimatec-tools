package com.agimatec.sql.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
@XStreamAlias("table")
public class TableDescription implements Serializable, Cloneable {
    private String comment;
    private String tableName;
    private List<ColumnDescription> columns =
        new ArrayList(); // list of ColumnDescription
    private IndexDescription primaryKey;
    private List<IndexDescription> indices = new ArrayList(); // list of IndexDescription
    private List<IndexDescription> constraints =
        new ArrayList(); // list of IndexDescription
    private List<ForeignKeyDescription> foreignKeys =
        new ArrayList(); // list of ForeignKeyDescription
    private String catalogName;
    private String schemaName;

    public TableDescription() {
    }

    /**
     * deep copy
     *
     * @return
     */
    public TableDescription deepCopy() {
        try {
            TableDescription clone = (TableDescription) clone();
            clone.columns = new ArrayList(columns.size());
            for (ColumnDescription each : columns) {
                clone.columns.add(each.deepCopy());
            }
            clone.indices = new ArrayList(indices.size());
            for (IndexDescription each : indices) {
                clone.indices.add(each.deepCopy());
            }
            clone.constraints = new ArrayList(constraints.size());
            for (IndexDescription each : constraints) {
                clone.constraints.add(each.deepCopy());
            }
            clone.foreignKeys = new ArrayList(foreignKeys.size());
            for (ForeignKeyDescription each : foreignKeys) {
                clone.foreignKeys.add(each.deepCopy());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getQualifiedTableName() {
        StringBuilder qualifiedTableName = new StringBuilder();
        if (getSchemaName() != null) {
            qualifiedTableName.append(getSchemaName());
            qualifiedTableName.append(".");
        }
        if (getCatalogName() != null) {
            qualifiedTableName.append(getCatalogName());
            qualifiedTableName.append(".");
        }
        qualifiedTableName.append(getTableName());
        return qualifiedTableName.toString();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String aTableName) {
        tableName = aTableName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String aComment) {
        comment = aComment;
    }

    public boolean isForeignKeyColumn(String columnName) {
        for (ForeignKeyDescription fk : getForeignKeys()) {
            if (fk.containsColumn(columnName)) return true;
        }
        return false;
    }

    public boolean isPrimaryKeyColumn(String columnName) {
        if (getPrimaryKey() == null) return false; // no primary key
        for (String pkCol : getPrimaryKey().getColumns()) {
            if (pkCol.equalsIgnoreCase(columnName)) return true;
        }
        return false;
    }

    public boolean isUnique(List<String> columns) {
        List<IndexDescription> indices = findIndicesForColumns(columns);
        for (IndexDescription index : indices) {
            if (index.isUnique()) return true;
        }
        return false;
    }

    public boolean isUnique(String column) {
        List<String> columns = new ArrayList(1);
        columns.add(column);
        return isUnique(columns);
    }

    public IndexDescription getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(IndexDescription aPrimaryKey) {
        primaryKey = aPrimaryKey;
    }

    public IndexDescription getIndex(String indexName) {
        for (IndexDescription indice : indices) {
            if (indice.getIndexName().equalsIgnoreCase(indexName)) return indice;
        }
        return null;
    }

    public void removeIndex(String indexName) {
        for (Iterator iter = indices.iterator(); iter.hasNext(); ) {
            IndexDescription theindexDescription = (IndexDescription) iter.next();
            if (theindexDescription.getIndexName().equalsIgnoreCase(indexName)) {
                iter.remove();
                return;
            }
        }
    }

    public IndexDescription getConstraint(String indexName) {
        for (IndexDescription indice : constraints) {
            if (indice.getIndexName().equalsIgnoreCase(indexName)) return indice;
        }
        return null;
    }

    public void removeConstraint(String indexName) {
        for (Iterator iter = constraints.iterator(); iter.hasNext(); ) {
            IndexDescription theindexDescription = (IndexDescription) iter.next();
            if (theindexDescription.getIndexName().equalsIgnoreCase(indexName)) {
                iter.remove();
                return;
            }
        }
    }

    public List<ForeignKeyDescription> getForeignKeys() {
        return foreignKeys;
    }

    public List<IndexDescription> getConstraints() {
        return constraints;
    }

    public List<IndexDescription> getIndices() {
        return indices;
    }

    public IndexDescription getIndex(int i) {
        return indices.get(i);
    }

    public int getIndexSize() {
        return indices.size();
    }

    public int getConstraintSize() {
        return constraints.size();
    }

    public void addIndex(IndexDescription aIndex) {
        indices.add(aIndex);
    }

    public void addConstraint(IndexDescription aIndex) {
        constraints.add(aIndex);
    }

    public IndexDescription getConstraint(int i) {
        return constraints.get(i);
    }

    public ForeignKeyDescription getForeignKey(String constraintName) {
        for (ForeignKeyDescription foreignKey : foreignKeys) {
            if (foreignKey.getConstraintName()
                .equalsIgnoreCase(constraintName)) return foreignKey;
        }
        return null;
    }

    /**
     * find a foreignKey in this table that has the same constraintName or columns/refColumns/refTable than 'other'
     *
     * @param other
     * @return the fk in this table or null if no similar fk found
     */
    public ForeignKeyDescription findForeignKeyLike(ForeignKeyDescription other) {
        if (other.getConstraintName() == null || other.getConstraintName().length() == 0) {
            for (ForeignKeyDescription foreignKey : foreignKeys) {
                if (foreignKey.isSimilarTo(other)) {
                    return foreignKey;
                }
            }
        } else {
            return getForeignKey(other.getConstraintName());
        }
        return null; // not found
    }

    public ForeignKeyDescription getForeignKey(int i) {
        return foreignKeys.get(i);
    }

    public int getForeignKeySize() {
        return foreignKeys.size();
    }

    public void addForeignKey(ForeignKeyDescription aFK) {
        foreignKeys.add(aFK);
    }

    /**
     * column names of this table in alphabetic order
     */
    public String[] getColumnNames() {
        String[] columnNames = new String[columns.size()];
        Iterator iter = columns.iterator();
        int i = 0;
        while (iter.hasNext()) {
            ColumnDescription theColumnDescription = (ColumnDescription) iter.next();
            columnNames[i++] = theColumnDescription.getColumnName();
        }
        Arrays.sort(columnNames);
        return columnNames;
    }

    /**
     * list of ColumnDescription
     *
     * @return
     */
    public List<ColumnDescription> getColumns() {
        return columns;
    }

    public ColumnDescription getColumn(int i) {
        return columns.get(i);
    }

    /**
     * @param columnName - name to search for (ignore case)
     * @return null or the columndescription
     */
    public ColumnDescription getColumn(String columnName) {
        for (ColumnDescription column : columns) {
            if (column.getColumnName().equalsIgnoreCase(columnName)) return column;
        }
        return null;
    }

    public void removeColumn(String columnName) {
        ColumnDescription colDesc = getColumn(columnName);
        if (colDesc != null) {
            columns.remove(colDesc);
        }
    }


    public int getColumnSize() {
        return columns.size();
    }

    public void addColumn(ColumnDescription aColumn) {
        columns.add(aColumn);
    }

    public String toString() {
        return getQualifiedTableName();
    }

    public IndexDescription findIndexForColumns(List<String> columns) {
        for (IndexDescription index : getIndices()) {
            if (index.isSameColumns(columns)) return index;
        }
        return null;
    }

    public IndexDescription findConstraintForColumns(List<String> columns) {
        if (primaryKey != null && primaryKey.isSameColumns(columns)) return primaryKey;
        for (IndexDescription index : getConstraints()) {
            if (index.isSameColumns(columns)) return index;
        }
        return null;
    }

    public List<IndexDescription> findIndicesForColumns(List<String> columns) {
        List<IndexDescription> indices = new ArrayList<IndexDescription>();
        if (getPrimaryKey() != null) {
            if (getPrimaryKey().isSameColumns(columns)) {
                indices.add(getPrimaryKey());
            }
        }
        for (IndexDescription index : getConstraints()) {
            if (index.isSameColumns(columns) && index.isUnique()) {
                indices.add(index);
            }
        }
        for (IndexDescription index : getIndices()) {
            if (index.isSameColumns(columns) && index.isUnique()) {
                indices.add(index);
            }
        }
        return indices;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
