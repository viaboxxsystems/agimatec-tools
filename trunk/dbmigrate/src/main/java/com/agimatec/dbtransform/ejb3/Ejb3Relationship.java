package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.ForeignKeyDescription;
import com.agimatec.sql.meta.TableDescription;
import org.apache.commons.lang.StringUtils;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 03.07.2007 <br/>
 * Time: 16:23:41 <br/>
 */
public class Ejb3Relationship extends Ejb3Prototype {
    protected final ForeignKeyDescription foreignKey;
    protected ColumnDescription column;
    protected ColumnDescription refColumn;
    protected Ejb3Class targetType;
    protected String attributeName;
    protected boolean optional = false;
    protected boolean oneToOne;
    private boolean primaryKeyJoin;

    /** @param foreignKey - fk of THIS side */
    public Ejb3Relationship(ForeignKeyDescription foreignKey) {
        this.foreignKey = foreignKey;
    }

    public ForeignKeyDescription getForeignKey() {
        return foreignKey;
    }

    public void generate(Ejb3Schema schema) {
        Ejb3Class targetEntity =
                schema.getEjb3classes().get(foreignKey.getRefTableName());
        if (targetEntity != null) {
            targetType = targetEntity;
            TableDescription table =
                    schema.getCatalog().getTable(foreignKey.getTableName());
            if (table != null) {
                column = table.getColumn(foreignKey.getColumn(0));
                oneToOne = table.isPrimaryKeyColumn(column.getColumnName());
                if (!oneToOne) {
                    oneToOne = table.isUnique(column.getColumnName());
                } else {
                    primaryKeyJoin = true;
                }
            }
            TableDescription refTable =
                    schema.getCatalog().getTable(foreignKey.getRefTableName());
            if (refTable != null)
                refColumn = refTable.getColumn(foreignKey.getRefColumn(0));
        } else { // unknown target type, substitute ...
            targetType = new Ejb3Class(foreignKey.getRefTableName());
        }
        if (column == null) {
            column = new ColumnDescription();
            column.setColumnName(foreignKey.getColumn(0));
        }
        if (refColumn == null) {
            refColumn = new ColumnDescription();
            refColumn.setColumnName(foreignKey.getRefColumn(0));
        }
        String colName = foreignKey.getColumn(0);
        if (colName.endsWith("_id")) colName = colName.substring(0, colName.length() - 3);
        attributeName = toProperAttributeName(colName);
    }

    public String getType() {
        return "ManyToOne";
    }

    public boolean isOneToOne() {
        return oneToOne;
    }

    public boolean isPrimaryKeyJoin() {
        return primaryKeyJoin;
    }

    public boolean isMapped() {
        return true;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getGetter() {
        return "get" + StringUtils.capitalize(getAttributeName());
    }

    public String getSetter() {
        return "set" + StringUtils.capitalize(getAttributeName());
    }

    public Ejb3Class getTargetType() {
        return targetType;
    }

    public boolean isToMany() {
        return false;
    }

    public String getJavaType() {
        return getTargetType().getClassName();
    }

    public String toString() {
        return attributeName;
    }

    public ColumnDescription getColumn() {
        return column;
    }

    public ColumnDescription getRefColumn() {
        return refColumn;
    }

    public void setOneToOne(boolean b) {
        oneToOne = true;
    }
}
