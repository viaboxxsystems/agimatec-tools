package com.agimatec.sql.meta.checking;

import com.agimatec.dbtransform.DataType;
import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.meta.*;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Description: Read database catalog with JDBC DatabaseMetaData APIs<br/>
 * User: roman.stumm <br/>
 * Date: 10.03.2008 <br/>
 * Time: 10:42:36 <br/>
 * Copyright: Agimatec GmbH
 */
public class JdbcSqlMetaFactory implements SqlMetaFactory {
    private final JdbcDatabase database;

    /**
     * default = true. load indices?
     */
    private boolean indices = true;
    /**
     * default = true. load primary key?
     * if true, requires indices to be true, otherwise this has no effect.
     */
    private boolean primaryKeys = true;
    /**
     * default = true. query columns?
     */
    private boolean columns = true;
    /**
     * default = true. query foreign keys
     */
    private boolean foreignKeys = true;

    public JdbcSqlMetaFactory(JdbcDatabase database) {
        this.database = database;
    }

    public JdbcDatabase getDatabase() {
        return database;
    }

    public boolean isColumns() {
        return columns;
    }

    public void setColumns(boolean columns) {
        this.columns = columns;
    }

    public boolean isForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(boolean foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public boolean isIndices() {
        return indices;
    }

    public void setIndices(boolean indices) {
        this.indices = indices;
    }

    public boolean isPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(boolean primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public void equalizeColumn(ColumnDescription cd) {
        // do nothing
    }

    /**
     * create a CatalogDescription
     */
    public CatalogDescription buildCatalog(String[] tables) throws SQLException, IOException {
        CatalogDescription catalog = new CatalogDescription();
        catalog.setSchemaName(getDatabase().getConnection().getCatalog());
        addTables(tables, catalog);
        return catalog;
    }

    protected final class TableIdentifier {
        String cat, schem, table;

        public TableIdentifier(String table) {
            int idx = table.indexOf('.');
            if (idx >= 0) {
                this.cat = table.substring(0, idx);
                if (idx < table.length() - 1) {
                    this.table = table.substring(idx + 1);
                }
            } else {
                this.table = table;
            }
        }

        public String getCat() {
            return cat;
        }

        public String getSchem() {
            return schem;
        }

        public String getTable() {
            return table;
        }

        public void setCat(String cat) {
            this.cat = cat;
        }

        public void setSchem(String schem) {
            this.schem = schem;
        }
    }

    protected void addTables(String[] tables, CatalogDescription aCatalog)
            throws SQLException, IOException {
        DatabaseMetaData meta = getDatabase().getConnection().getMetaData();
        Map<String, DataType> types = loadTypes(meta);
        for (String table : tables) {
            TableIdentifier tid = createTableIdentifier(table);
            ResultSet tableSet = meta.getTables(tid.getCat(), tid.getSchem(), tid.getTable(),
                    new String[]{"TABLE"});
            while (tableSet.next()) {
                String cat = tableSet.getString("TABLE_CAT");
                tid.setCat(cat);
                String schem = tableSet.getString("TABLE_SCHEM");
                tid.setSchem(schem);
                TableDescription td = createTable(tid);
                aCatalog.addTable(td);
                td.setComment(tableSet.getString("REMARKS"));
                if (primaryKeys) loadPrimaryKey(meta, tid, td);
                if (columns) loadColumns(meta, types, tid, td);
                if (foreignKeys) loadForeignKeys(meta, tid, td);
                if (indices) loadIndexes(meta, tid, td);
            }
        }
    }

    protected TableIdentifier createTableIdentifier(String table) {
        return new TableIdentifier(table);
    }

    protected void loadIndexes(DatabaseMetaData meta, TableIdentifier tid, TableDescription td)
            throws SQLException {
        ResultSet indSet =
                meta.getIndexInfo(tid.getCat(), tid.getSchem(), tid.getTable(), false, true);
        IndexDescription id = null;
        while (indSet.next()) {
            String indName = indSet.getString("INDEX_NAME");
            if (id == null || (id.getIndexName() != null && !id.getIndexName().equals(indName))) {
                id = new IndexDescription();
                id.setIndexName(indName);
                id.setUnique(!indSet.getBoolean("NON_UNIQUE"));
                id.setTableName(indSet.getString("TABLE_NAME"));
                if (td.getPrimaryKey() == null ||
                        !id.getIndexName().equals(td.getPrimaryKey().getIndexName())) {
                    td.addIndex(id);
                }
            }
            id.addColumn(indSet.getString("COLUMN_NAME"),
                    ("D".equals(indSet.getString("ASC_OR_DESC"))) ? "DESC" : "ASC");
        }
    }


    protected void loadForeignKeys(DatabaseMetaData meta, TableIdentifier tid, TableDescription td)
            throws SQLException {
        ResultSet fkSet = meta.getImportedKeys(tid.getCat(), tid.getSchem(), tid.getTable());
        ForeignKeyDescription fkDesc = null;
        while (fkSet.next()) {
            String fkName = fkSet.getString("FK_NAME");
            if (fkDesc == null || (fkDesc.getConstraintName() != null &&
                    !fkDesc.getConstraintName().equals(fkName))) {
                fkDesc = new ForeignKeyDescription();
                fkDesc.setTableName(fkSet.getString("FKTABLE_NAME"));
                fkDesc.setConstraintName(fkName);
                short i = fkSet.getShort("DELETE_RULE");
                switch (i) {
                    case 0:
                        fkDesc.setOnDeleteRule("CASCADE");
                        break;
                    case 3:
                    default:
                        fkDesc.setOnDeleteRule(null);
                }
                fkDesc.setRefTableName(fkSet.getString("PKTABLE_NAME"));
                td.addForeignKey(fkDesc);
            }
            fkDesc.addColumnPair(fkSet.getString("FKCOLUMN_NAME"),
                    fkSet.getString("PKCOLUMN_NAME"));
        }
    }


    protected Map<String, DataType> loadTypes(DatabaseMetaData meta) throws SQLException {
        Map<String, DataType> types = new HashMap();
        ResultSet typeSet = meta.getTypeInfo();
        while (typeSet.next()) {
            DataType dt = new DataType();
            dt.setTypeName(typeSet.getString("TYPE_NAME"));
            dt.setPrecision(typeSet.getInt("PRECISION"));
            dt.setScale(new Integer(typeSet.getShort("MAXIMUM_SCALE")));
            dt.setPrecisionEnabled(dt.getPrecision() > 0);
            types.put(dt.getTypeName(), dt);
        }
        return types;
    }

    protected void loadColumns(DatabaseMetaData meta, Map<String, DataType> types,
                               TableIdentifier tid, TableDescription td) throws SQLException {
        ResultSet colSet = meta.getColumns(tid.getCat(), tid.getSchem(), tid.getTable(), null);
        while (colSet.next()) {
            ColumnDescription cd = new ColumnDescription();
            cd.setColumnName(colSet.getString("COLUMN_NAME"));
            td.addColumn(cd);
            cd.setComment(colSet.getString("REMARKS"));
            cd.setTypeName(colSet.getString("TYPE_NAME"));
            DataType dt = types.get(cd.getTypeName());
            if (dt != null) {
                if (dt.isPrecisionEnabled() != null && dt.isPrecisionEnabled().booleanValue()) {
                    cd.setPrecision(colSet.getInt("COLUMN_SIZE"));
                }
                if (dt.getScale() != null && dt.getScale().intValue() != 0) {
                    cd.setScale(colSet.getInt("DECIMAL_DIGITS"));
                }
            }
            cd.setNullable(colSet.getInt("NULLABLE") == 1);
            cd.setDefaultValue(colSet.getString("COLUMN_DEF"));
            equalizeColumn(cd);
        }
    }

    protected void loadPrimaryKey(DatabaseMetaData meta, TableIdentifier tid, TableDescription td)
            throws SQLException {
        ResultSet colSet = meta.getPrimaryKeys(tid.getCat(), tid.getSchem(), tid.getTable());
        Map<Integer, String> pkColSeq = new HashMap();
        if (colSet.next()) {
            IndexDescription pk = new IndexDescription();
            td.setPrimaryKey(pk);
            pk.setIndexName(colSet.getString("PK_NAME"));
            pk.setUnique(true);
            do {
                pkColSeq.put(colSet.getInt("KEY_SEQ"), colSet.getString("COLUMN_NAME"));
            } while (colSet.next());

            List<Integer> keys = new ArrayList(pkColSeq.keySet());
            Collections.sort(keys);
            for (Integer seq : keys) {
                pk.addColumn(pkColSeq.get(seq));
            }
        }
    }

    protected TableDescription createTable(TableIdentifier tableIdentifier) throws SQLException {
        final TableDescription tableDesc = new TableDescription();
        StringBuilder qualifiedTableName = new StringBuilder();
        if (tableIdentifier.getSchem() != null) {
            qualifiedTableName.append(tableIdentifier.getSchem());
            qualifiedTableName.append(".");
        }
        if (tableIdentifier.getCat() != null) {
            qualifiedTableName.append(tableIdentifier.getCat());
            qualifiedTableName.append(".");
        }
        qualifiedTableName.append(tableIdentifier.getTable());
        tableDesc.setTableName(qualifiedTableName.toString().toUpperCase());
        return tableDesc;
    }

    /* protected void printAll(String title, ResultSet colSet) throws SQLException {
        int c = colSet.getMetaData().getColumnCount();
        List<String> colNames  = new ArrayList();
        for(int i=1;i<=c;i++) {
            String col = colSet.getMetaData().getColumnName(i);
            colNames.add(col);
        }
        Collections.sort(colNames);
        System.out.println("############# " + title + " ###############");

        while(colSet.next()) {
            for(String col : colNames) {
                System.out.print(col + " = " + colSet.getString(col)  + "\n");
            }
            System.out.println("\n----------------------------------------------------");
        }
    }*/
}
