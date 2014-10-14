package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.ForeignKeyDescription;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.07.2007 <br/>
 * Time: 16:24:44 <br/>
 */
public class Ejb3RelationshipManyToMany extends Ejb3RelationshipOneToMany {
    private final ForeignKeyDescription otherForeignKey;

    /**
     * @param foreignKey - fk of OTHER side
     */
    public Ejb3RelationshipManyToMany(ForeignKeyDescription thisForeignKey,
                                      ForeignKeyDescription foreignKey) {
        super(thisForeignKey);
        this.otherForeignKey = foreignKey;
    }

    public ForeignKeyDescription getOtherForeignKey() {
        return otherForeignKey;
    }

    @Override
    public void generate(Ejb3Schema schema) {
        Ejb3Class targetEntity = schema.getEjb3classes().get(otherForeignKey.getRefTableName());
        if (targetEntity != null) {
            targetType = targetEntity;
            /*TableDescription table =
                    schema.getCatalog().getTable(foreignKey.getTableName());
            if (table != null) column = table.getColumn(foreignKey.getColumn(0));
            TableDescription refTable =
                    schema.getCatalog().getTable(foreignKey.getRefTableName());
            if (refTable != null)
                refColumn = refTable.getColumn(foreignKey.getRefColumn(0));*/
        } else { // unknown target type, substitute ...
            targetType = new Ejb3Class(otherForeignKey.getRefTableName());
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
        attributeName = toPlural(toProperAttributeName(targetType.getClassName()));
    }

    @Override
    public String getType() {
        return "ManyToMany";
    }

}
