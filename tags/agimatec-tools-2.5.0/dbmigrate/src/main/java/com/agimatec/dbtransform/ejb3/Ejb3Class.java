package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.ForeignKeyDescription;
import com.agimatec.sql.meta.IndexDescription;
import com.agimatec.sql.meta.TableDescription;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 03.07.2007 <br/>
 * Time: 16:20:11 <br/>
 * Copyright: Agimatec GmbH
 */
public class Ejb3Class extends Ejb3Prototype {
    private static final Log log = LogFactory.getLog(Ejb3Class.class);
    private final TableDescription table;
    private final List<Ejb3Attribute> attributes = new ArrayList();
    private final List<Ejb3Relationship> relationships = new ArrayList();
    private final String className;

    public Ejb3Class(TableDescription tableDescription) {
        this.table = tableDescription;
        className = toProperEntityName(table.getTableName().toLowerCase());
    }

    protected Ejb3Class(String name) {
        this.table = null;
        className = toProperEntityName(name);
    }

    public TableDescription getTable() {
        return table;
    }

    public String getClassName() {
        return className;
    }

    public void generateAttributes() {
        for (ColumnDescription column : table.getColumns()) {
            if (!table.isForeignKeyColumn(column.getColumnName())) {
                Ejb3Attribute attribute = new Ejb3Attribute(column);
                attribute.generate();
                attributes.add(attribute);
            }
        }
    }

    public List<List<String>> getMultiUniqueConstraints() {
        List<List<String>> uniqueCons = new ArrayList();
        for (IndexDescription index : table.getConstraints()) {
            if (index.isUnique() && index.getColumnSize() > 1) {
                uniqueCons.add(index.getColumns());
            }
        }
        for (IndexDescription index : table.getIndices()) {
            if (index.isUnique() && index.getColumnSize() > 1) {
                if (!uniqueCons.contains(index.getColumns())) {
                    uniqueCons.add(index.getColumns());
                }
            }
        }
        return uniqueCons;
    }

    public void generateRelationships(Ejb3Schema ejb3Schema) {
        if (isManyToManyLink()) {
            Ejb3RelationshipManyToMany relationship =
                    new Ejb3RelationshipManyToMany(table.getForeignKey(0), table.getForeignKey(1));
            relationship.setOptional(false);
            relationship.generate(ejb3Schema);
            Ejb3Class ejb3Class = ejb3Schema.getEjb3classes()
                    .get(relationship.getForeignKey().getRefTableName());
            ejb3Class.getRelationships().add(relationship);
        } else {
            for (ForeignKeyDescription fk : table.getForeignKeys()) {
                Ejb3Relationship relationship = new Ejb3Relationship(fk);
                relationship.generate(ejb3Schema);
                relationships.add(relationship);

                // decision: create other side?
                Ejb3Class ejb3Class = ejb3Schema.getEjb3classes().get(fk.getRefTableName());
                if (ejb3Class != null) {
                    Ejb3RelationshipOneToMany otherSide = new Ejb3RelationshipOneToMany(fk);
                    if (relationship.isOneToOne()) otherSide.setOneToOne(true);
                    otherSide.setMappedByRelationship(relationship);
                    otherSide.generate(ejb3Schema);
                    ejb3Class.getRelationships().add(otherSide);
                } else {
                   log.warn("cannot find referenced class for table " + fk.getRefTableName() + " at foreign key " + fk);
                }
            }
        }
    }

    public List<Ejb3Attribute> getAttributes() {
        return attributes;
    }

    public List<Ejb3Relationship> getRelationships() {
        return relationships;
    }

    public String toString() {
        return className;
    }

    /** two identifiying foreign keys and no other attributes */
    public boolean isManyToManyLink() {
        if (table.getColumnSize() != 2) return false;

        for (ColumnDescription column : table.getColumns()) {
            if (!table.isForeignKeyColumn(column.getColumnName())) {
                return false;
            }
            if (!table.isPrimaryKeyColumn(column.getColumnName())) {
                return false;
            }
        }
        return true;
    }
}
