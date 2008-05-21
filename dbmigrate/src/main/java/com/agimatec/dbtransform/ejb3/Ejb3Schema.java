package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.TableDescription;

import java.util.*;

/**
 * Features:
 * * Generate EJB3 classes from a database {@link CatalogDescription}
 * * generate multiple .java files
 * * generate class per table
 * * generate attribute per column (except fk-columns)
 * * generate ManyToOne-relationship per foreignkey
 * * mappedBy OneToMany relationship,
 * * ManyToMany relationship autodetect and generated,
 * * add, remove methods
 * * autodetect caching strategies tableName.startsWith(cv) : READ_ONLY [HEURISTIC]
 * * autodetect Enum types (?) [HEURISTIC]
 * * Hibernate annotations (DELETE_CASCADE)
 * * unique attribute for @Column and @JoinColumn,
 * * multicolumn-uniqueconstraint annotations
 * <p/>
 * <p/>
 * Not supported:
 * * external configuration
 * <p/>
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 03.07.2007 <br/>
 * Time: 16:20:04 <br/>
 * Copyright: Agimatec GmbH
 */
public class Ejb3Schema extends Ejb3Prototype {
    private final CatalogDescription catalog;
    private Map<String, Ejb3Class> ejb3classes;

    public Ejb3Schema(CatalogDescription catalog) {
        this.catalog = catalog;
    }

    public CatalogDescription getCatalog() {
        return catalog;
    }

    public void generate() {
        ejb3classes = new HashMap<String, Ejb3Class>();
        List<Ejb3Class> manyToManyLinks = new ArrayList();
        for (TableDescription table : catalog.getTables().values()) {
            Ejb3Class ejb3 = new Ejb3Class(table);
            if (!ejb3.isManyToManyLink()) {
                ejb3.generateAttributes();
                ejb3classes.put(ejb3.getTable().getTableName(), ejb3);
            } else {
                manyToManyLinks.add(ejb3);
            }
        }
        for (Ejb3Class ejb3 : ejb3classes.values()) {
            ejb3.generateRelationships(this);
        }
        for (Ejb3Class ejb3 : manyToManyLinks) {
            ejb3.generateRelationships(this);
        }
    }

    public Map<String, Ejb3Class> getEjb3classes() {
        return ejb3classes;
    }

    public Collection<Ejb3Class> getEjb3classesCollection() {
        return ejb3classes.values();
    }
}
