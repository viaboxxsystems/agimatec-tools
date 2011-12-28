package com.agimatec.sql.meta.oracle;

import com.agimatec.jdbc.JdbcDatabase;
import com.agimatec.sql.meta.*;
import com.agimatec.sql.query.JdbcQueryUtil;
import com.agimatec.sql.query.QueryDefinition;
import com.agimatec.sql.query.SQLBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * <b>Description:</b>
 * Builds a CatalogDescription by reading the Schema-Catalog tables of an oracle database.<br>
 *
 * @author Roman Stumm
 */
public class OracleJdbcSqlMetaFactory implements SqlMetaFactory {
    private final JdbcDatabase database;
    private JdbcQueryUtil queryUtil;

    /** switches: sequences (true= load sequences into catalog, default = false) */
    private boolean sequences = false;
    /** default = true. load indices? */
    private boolean indices = true;
    /**
     * default = true. load primary key?
     * if true, requires indices to be true, otherwise this has no effect.
     */
    private boolean primaryKeys = true;
    /** default = true. query columns? */
    private boolean columns = true;
    /** default = true. query foreign keys */
    private boolean foreignKeys = true;

    /** default = false. query comments for tables, columns, constraints */
    private boolean comments = false;

    public boolean isSequences() {
        return sequences;
    }

    public void setSequences(boolean aSequences) {
        sequences = aSequences;
    }

    public boolean isIndices() {
        return indices;
    }

    public void setIndices(boolean aIndices) {
        indices = aIndices;
    }

    public boolean isPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(boolean aPrimaryKeys) {
        primaryKeys = aPrimaryKeys;
    }

    public boolean isColumns() {
        return columns;
    }

    public void setColumns(boolean aColumns) {
        columns = aColumns;
    }

    public boolean isForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(boolean aForeignKeys) {
        foreignKeys = aForeignKeys;
    }

    public boolean isComments() {
        return comments;
    }

    public void setComments(boolean aComments) {
        comments = aComments;
    }

    /**
     * create an instance
     *
     * @param aDatabase - to get the database connection from (no mappings required)
     */
    public OracleJdbcSqlMetaFactory(JdbcDatabase aDatabase) {
        database = aDatabase;
    }

    /**
     * load the catalog for ALL USER_TABLES!
     *
     * @throws java.sql.SQLException
     */
    public CatalogDescription buildCatalog() throws SQLException, IOException,
            SAXException {
        return buildCatalog(loadTables());
    }

    /** create a CatalogDescription */
    public CatalogDescription buildCatalog(String[] tables) throws SQLException,
            IOException {
        CatalogDescription catalog = new CatalogDescription();
        catalog.setSchemaName(getSchemaName());
        addTables(tables, catalog);
        if (sequences && catalog.getSchemaName() != null) {
            addSequences(catalog);
        }
        return catalog;
    }

    private String getSchemaName() throws SQLException, IOException {
        QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setMaxResults(1);
        queryDefinition.setQueryName("load-user");
        return (String) getQueryBean().executeQuery(queryDefinition).getFirst();
    }

    private void addSequences(CatalogDescription aCatalog) throws SQLException,
            IOException {
        SequenceDescription[] sequences = loadSequences(aCatalog.getSchemaName());
        for (SequenceDescription sequence : sequences) {
            aCatalog.addSequence(sequence);
        }
    }

    private void addTables(String[] tables, CatalogDescription aCatalog)
            throws SQLException, IOException {
        if (foreignKeys) {
            for (String table1 : tables) {
                TableDescription table = createTable(table1);
                aCatalog.addTable(table);
                final ForeignKeyDescription[] fks = loadForeignKeys(table.getTableName());
                for (ForeignKeyDescription fk : fks) {
                    table.addForeignKey(fk);
                }
            }
            if (comments && foreignKeys) { // build constraints comments

            }
        }
        if (columns) buildColumns(aCatalog);
        if (indices) buildIndexes(aCatalog);
        if (indices && primaryKeys) buildPrimaryKeys(
                aCatalog); // must be called after buildIndices, moves PK index to primaryKey in all tables
        if (columns) {
            for (String table1 : tables) {
                TableDescription table = aCatalog.getTable(table1);
                if (table.getColumnSize() == 0) {
                    aCatalog.removeTable(table1);
                }
            }
        }
        if (comments) { // build comments
            loadTableComments(aCatalog);
            if (foreignKeys) loadFKComments(aCatalog);
            if (columns) loadColumnComments(aCatalog);
        }
    }

    private void loadColumnComments(CatalogDescription aCatalog) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(aCatalog);
        queryDefinition.setQueryName("column_comments");
        getQueryBean().executeQuery(queryDefinition);
    }

    private void loadFKComments(CatalogDescription aCatalog) {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(aCatalog);
        queryDefinition.setQueryName("constraint_comments");
        try {
            getQueryBean().executeQuery(queryDefinition);
        } catch (Exception ex) {
            // ignore, because constraint_comments table is not a standard table in oracle
        }
    }

    private void loadTableComments(CatalogDescription aCatalog) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(aCatalog);
        queryDefinition.setQueryName("table_comments");
        getQueryBean().executeQuery(queryDefinition);
    }

    private ForeignKeyDescription[] loadForeignKeys(String tablename) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(tablename.toUpperCase());
        queryDefinition.setQueryName("foreign-keys-for-table");
        final List l = getQueryBean().executeQuery(queryDefinition).getList();
        return (ForeignKeyDescription[]) l.toArray(new ForeignKeyDescription[l.size()]);
    }

    private TableDescription createTable(String tableName) throws SQLException {
        final TableDescription tableDesc = new TableDescription();
        tableDesc.setTableName(tableName.toUpperCase());
        return tableDesc;
    }

    private void buildColumns(CatalogDescription aCatalog) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(aCatalog);
        queryDefinition.setQueryName("user-columns");
        getQueryBean().executeQuery(queryDefinition).getList();
    }

    private void buildPrimaryKeys(CatalogDescription aCatalog) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(aCatalog);
        queryDefinition.setQueryName("user-primary-keys");
        getQueryBean().executeQuery(queryDefinition).getList();
    }

    private void buildIndexes(CatalogDescription aCatalog) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(aCatalog);
        queryDefinition.setQueryName("user-indices");
        getQueryBean().executeQuery(queryDefinition).getList();
    }

    private SequenceDescription[] loadSequences(String owner) throws SQLException,
            IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryObject(owner);
        queryDefinition.setQueryName("all-sequences-for-owner");
        final List l = getQueryBean().executeQuery(queryDefinition).getList();
        return (SequenceDescription[]) l.toArray(new SequenceDescription[l.size()]);
    }

    private String[] loadTables() throws SQLException, IOException {
        final QueryDefinition queryDefinition = new QueryDefinition();
        queryDefinition.setQueryName("load-tables");
        final List l = getQueryBean().executeQuery(queryDefinition).getList();
        return (String[]) l.toArray(new String[l.size()]);
    }

    private JdbcQueryUtil getQueryBean() throws IOException {
        if (queryUtil == null) {
            queryUtil = new JdbcQueryUtil(getDatabase().getConnection(), new SQLBuilder(
                    "com/agimatec/sql/meta/oracle-statements.properties"));
        }
        return queryUtil;
    }

    public JdbcDatabase getDatabase() {
        return database;
    }
}
