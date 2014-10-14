package com.agimatec.sql.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Description:</b>   Description of an index in the database<br>
 *
 * @author Roman Stumm
 */
@XStreamAlias("index")
public class IndexDescription extends A_IntegrityRuleDescription {
    protected String indexName; // null or the index name for that rule
    private List<String> columns = new ArrayList(); // list of String , indexed columns
    private List<String> columnsSortDirections =
            new ArrayList();  // list of Ordering (Ordering.ASC, Ordering.DESC)
    private boolean unique;
    private boolean reverse;
    private boolean noSort;
    private boolean bitmap;
    private boolean context;
    private boolean functionBased;

    public IndexDescription deepCopy() {
        try {
            IndexDescription clone = (IndexDescription) clone();
            clone.columns = new ArrayList(columns);
            clone.columnsSortDirections = new ArrayList(columnsSortDirections);
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String aIndexName) {
        indexName = aIndexName;
    }

    /**
     * list of Strings (column names)
     *
     * @return
     */
    public List<String> getColumns() {
        return columns;
    }

    public String getColumn(int i) {
        return columns.get(i);
    }

    public int getColumnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            String theColumnName = columns.get(i);
            if (theColumnName.equalsIgnoreCase(columnName)) return i;
        }
        return -1;
    }

    /**
     * add a column in ascending order (=default order)
     *
     * @param aCol
     */
    public void addColumn(String aCol) {
        addColumn(aCol, "ASC");
    }

    /**
     * @param aCol  - column name
     * @param order - OrderClause.ASC or OrderClause.DESC
     */
    public void addColumn(String aCol, String order) {
        columns.add(aCol);
        columnsSortDirections.add(order);
    }

    public String getOrderDirection(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            String theColumnDescription =  columns.get(i);
            if (theColumnDescription.equalsIgnoreCase(columnName)) {
                return columnsSortDirections.get(i);
            }
        }
        return "ASC";
    }

    /**
     * @param i
     * @return "ASC" or "DESC"
     */
    public String getOrderDirection(int i) {
        return columnsSortDirections.get(i);
    }

    public int getColumnSize() {
        return columns.size();
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean aUnique) {
        unique = aUnique;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean aReverse) {
        reverse = aReverse;
    }

    public boolean isNoSort() {
        return noSort;
    }

    public void setNoSort(boolean aNoSort) {
        noSort = aNoSort;
    }

    public boolean isBitmap() {
        return bitmap;
    }

    public void setBitmap(boolean aBitmap) {
        bitmap = aBitmap;
    }

    public boolean isContext() {
        return context;
    }

    public void setContext(boolean aContext) {
        context = aContext;
    }

    public boolean isFunctionBased() {
        return functionBased;
    }

    public void setFunctionBased(boolean aFunctionBased) {
        functionBased = aFunctionBased;
    }

    public String toString() {
        return getIndexName();
    }

    public boolean isSameColumns(List<String> otherColumns)
    {
        if(otherColumns.size() != columns.size()) return false;
        for(String each : columns) {
            if(!otherColumns.contains(each)) return false;
        }
        return true;
    }
}
