package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.ForeignKeyDescription;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.07.2007 <br/>
 * Time: 15:45:14 <br/>
 * Copyright: Agimatec GmbH
 */
public class Ejb3RelationshipOneToMany extends Ejb3Relationship {
    private Ejb3Relationship mappedByRelationship;

    /** @param foreignKey - fk of OTHER side */
    public Ejb3RelationshipOneToMany(ForeignKeyDescription foreignKey) {
        super(foreignKey);
        setOptional(true);
    }

    @Override
    public void generate(Ejb3Schema schema) {
        Ejb3Class targetEntity = schema.getEjb3classes().get(foreignKey.getTableName());
        if (targetEntity != null) {
            targetType = targetEntity;
            /* TableDescription table =
            schema.getCatalog().getTable(foreignKey.getTableName());
    if (table != null) column = table.getColumn(foreignKey.getColumn(0));
    TableDescription refTable =
            schema.getCatalog().getTable(foreignKey.getRefTableName());
    if (refTable != null)
        refColumn = refTable.getColumn(foreignKey.getRefColumn(0));  */
        } else { // unknown target type, substitute ...
            targetType = new Ejb3Class(foreignKey.getTableName());
        }
        /*if (column == null) {
            column = new ColumnDescription();
            column.setColumnName(foreignKey.getColumn(0));
        }
        if (refColumn == null) {
            refColumn = new ColumnDescription();
            refColumn.setColumnName(foreignKey.getRefColumn(0));
        } */

        // build the plural
        if (!isOneToOne()) {
            attributeName = toPlural(toProperAttributeName(targetType.getClassName()));
        } else {
            attributeName = toProperAttributeName(targetType.getClassName());
        }
    }

    public boolean isMapped() {
        return mappedByRelationship != null;
    }

    public Ejb3Relationship getMappedByRelationship() {
        return mappedByRelationship;
    }

    public void setMappedByRelationship(Ejb3Relationship mappedByRelationship) {
        this.mappedByRelationship = mappedByRelationship;
    }

    @Override
    public String getType() {
        return "OneToMany";
    }

    @Override
    public String getJavaType() {
        if (isOneToOne()) return super.getJavaType();
        else return "List<" + getTargetType().getClassName() + ">";
    }

    @Override
    public boolean isToMany() {
        return true;
    }
}
