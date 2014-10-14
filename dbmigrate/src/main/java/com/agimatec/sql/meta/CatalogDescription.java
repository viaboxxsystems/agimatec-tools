package com.agimatec.sql.meta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.*;

/**
 * <b>Description:</b>   root object that contains the database schema description<br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 *
 * @author Roman Stumm
 */
@XStreamAlias("catalog")
public class CatalogDescription implements Serializable, Cloneable {
    private Map<String, TableDescription> tables; // key = table name, value = TableDescription
    private Map<String, SequenceDescription> sequences; // key = sequence name, value = SequenceDescription
    private String schemaName;
    // indexes are grouped under the table they belong to

    public CatalogDescription() {
        tables = new HashMap();
        sequences = new HashMap();
    }

    public CatalogDescription deepCopy() {
        try {
            CatalogDescription clone = (CatalogDescription) clone();
            clone.tables = new HashMap(tables.size());
            for (TableDescription each : tables.values()) {
                clone.addTable(each.deepCopy());
            }
            clone.sequences = new HashMap(sequences.size());
            for (SequenceDescription each : sequences.values()) {
                clone.addSequence(each.deepCopy());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public TableDescription getTable(String tableName) {
        return tables.get(tableName.toUpperCase());
    }

    public SequenceDescription getSequence(String seqName) {
        return sequences.get(seqName);
    }

    public void addTable(TableDescription aTD) {
        tables.put(aTD.getTableName().toUpperCase(), aTD);
    }

    public void addSequence(SequenceDescription aSD) {
        sequences.put(aSD.getSequenceName(), aSD);
    }

    public void removeSequence(String seqName) {
        sequences.remove(seqName);
    }

    public int getTablesSize() {
        return tables.size();
    }

    public int getSequencesSize() {
        return sequences.size();
    }

    /** @return collection of TableDescription */
    public Map<String, TableDescription> getTables() {
        return tables;
    }

    public Collection<SequenceDescription> getSequences() {
        return sequences.values();
    }

    public Collection<TableDescription> getTableCollection() {
        return tables.values();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String aSchemaName) {
        schemaName = aSchemaName;
    }

    /** @return all table names in this catalog in alphabetic order */
    public String[] getTableNames() {
        String[] theTables = new String[getTables().size()];
        int pos = 0;
        for (TableDescription tableDescription : getTables().values()) {
            theTables[pos++] = tableDescription.getTableName();
        }
        Arrays.sort(theTables);
        return theTables;
    }

    public void removeTable(String tableName) {
        tables.remove(tableName.toUpperCase());
    }

    /** return all tables that directly reference the given table */
    public ForeignKeyDescription[] getForeignKeysReferencing(String tableName) {
        final List referrers = new ArrayList();

        for (TableDescription tableDescription : getTables().values()) {
            for (int fkIdx = 0; fkIdx < tableDescription.getForeignKeySize(); fkIdx++) {
                ForeignKeyDescription fk = tableDescription.getForeignKey(fkIdx);
                if (tableName.equalsIgnoreCase(fk.getRefTableName())) {
                    referrers.add(fk);
                }
            }
        }
        return (ForeignKeyDescription[]) referrers
                .toArray(new ForeignKeyDescription[referrers.size()]);
    }

    /**
     * extract the tablenames from the given ruledescriptions
     *
     * @return of of Strings: alphabetically sorted array of table names (each name is unique in the array)
     */
    public static List getTableNames(A_IntegrityRuleDescription[] rules) {
        Set tableNames = new HashSet(rules.length);
        for (A_IntegrityRuleDescription theRule : rules) {
            tableNames.add(theRule.getTableName());
        }
        final List list = new ArrayList(tableNames);
        Collections.sort(list);
        return list;
    }
}

