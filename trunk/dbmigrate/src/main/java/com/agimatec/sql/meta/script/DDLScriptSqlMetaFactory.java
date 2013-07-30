package com.agimatec.sql.meta.script;

import com.agimatec.commons.config.MapNode;
import com.agimatec.dbmigrate.action.ScriptAction;
import com.agimatec.sql.meta.*;
import com.agimatec.sql.script.SQLScriptParser;
import com.agimatec.sql.script.ScriptVisitor;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 24.04.2007 <br/>
 * Time: 19:01:18 <br/>
 * Copyright: Agimatec GmbH
 */
public class DDLScriptSqlMetaFactory implements SqlMetaFactory, ScriptVisitor {
    private static final Logger log = LoggerFactory.getLogger(DDLScriptSqlMetaFactory.class);
    private CatalogDescription catalog;
    private final PropertiesExtractor extractor;

    private final Map<String, CatalogBuilder> builders = new HashMap<String, CatalogBuilder>();
    private final DDLExpressions ddlSpec;

    public DDLScriptSqlMetaFactory(DDLExpressions ddlSpecification) {
        init();
        ddlSpec = ddlSpecification;
        if (ddlSpec.getExpressions() == null) throw new IllegalStateException(
                "DDL class not ready - initialization failed");
        extractor = new PropertiesExtractor();
        if (log.isDebugEnabled()) {
            log.debug("using " + ddlSpecification);
        }
    }

    protected void init() {
        builders.put("table-add-columns", new TableAddColumnsBuilder());
        builders.put("table-alter-columns", new TableAlterColumnsBuilder());
        builders.put("table-add-constraint", new TableAddConstraintBuilder());
        builders.put("create-index", new CreateIndexBuilder());
        builders.put("table-add-foreign-key", new TableAddForeignKeyBuilder());
        builders.put("create-sequence", new CreateSequenceBuilder());
        builders.put("create-table", new CreateTableBuilder());
        builders.put("drop-table", new DropTableBuilder());
        builders.put("drop-sequence", new DropSequenceBuilder());
        builders.put("dezign-create-table", new DezignCreateTableBuilder());
        builders.put("table-add-primary-key", new TableAddPrimaryKey());
        builders.put("table-comment", new TableCommentBuilder());
        builders.put("column-comment", new ColumnCommentBuilder());
    }

    public static ExtractExpr[] compileExpressions(String[] statementFormats) {
        final ExtractExpr[] expressions = new ExtractExpr[statementFormats.length];
        for (int i = 0; i < expressions.length; i++) {
            String format = statementFormats[i];
            try {
                expressions[i] = ExtractExprBuilder.buildExpr(format);
            } catch (ParseException e) {
                log.error("cannot initialize expression: " + format, e);
                return null;
            }
        }
        return expressions;
    }

    protected Map<String, CatalogBuilder> getBuilders() {
        return builders;
    }

    protected DDLExpressions getDdlSpec() {
        return ddlSpec;
    }

    protected PropertiesExtractor getExtractor() {
        return extractor;
    }

    protected abstract class CatalogBuilder {
        public abstract void process(MapNode values, CatalogDescription catalog) throws
                IOException, TemplateException;

        /**
         * remove \"
         *
         * @param value - value or null to strip
         * @return stripped value or null
         */
        protected String strip(String value) {
            return ddlSpec.strip(value);
        }

        protected String unqualified(String value) {
            int idx = value.lastIndexOf('.');
            if (idx >= 0) {
                return value.substring(idx + 1);
            } else {
                return value;
            }
        }

        protected int getInt(MapNode values, String path) {
            String s = values.getString(path);
            if (s != null && s.length() > 0) try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                return 0;
            }
            else return 0;
        }

        protected boolean getBool(MapNode values, String path) {
            String v = values.getString(path);
            return v != null && v.length() > 0;
        }

        protected ColumnDescription buildColumnDescription(MapNode aColDef,
                                                           TableDescription aTd) {
            ColumnDescription cd = new ColumnDescription();
            cd.setColumnName(strip(aColDef.getString("column")));
            cd.setNullable(!getBool(aColDef, "mandatory"));
            cd.setComment((String) aColDef.get("comment"));
            setColType(aColDef, cd);
            aTd.addColumn(cd);
            if (aColDef.getString("isPK") != null) {
                IndexDescription pk = aTd.getPrimaryKey();
                if (pk == null) {
                    pk = new IndexDescription();
                    pk.setTableName(aTd.getTableName());
                    pk.setUnique(true);
                    aTd.setPrimaryKey(pk);
                }
                pk.addColumn(cd.getColumnName());
            }
            return cd;
        }

        protected void setColType(MapNode aColDef, ColumnDescription cd) {
            Map precision = aColDef.getMap("precision");
            if (precision != null) {
                cd.setPrecisionEnabled(true);
                List numbers = (List) precision.get("numbers");
                if (numbers.size() > 0) {
                    cd.setPrecision(getInt(aColDef, "precision/numbers/0/value"));
                }
                if (numbers.size() > 1) {
                    cd.setScale(getInt(aColDef, "precision/numbers/1/value"));
                }
            }
            cd.setTypeName(aColDef.getString("typeName"));
            if (aColDef.getString("unsigned") != null) {
                cd.setTypeName(cd.getTypeName() + " " + aColDef.getString("unsigned"));
            }
            if (aColDef.getString("varying") != null) {
                cd.setTypeName(cd.getTypeName() + " " + aColDef.getString("varying"));
            }
            cd.setDefaultValue(aColDef.getString("default/defaultValue"));
            ddlSpec.equalizeColumn(cd);
        }

        protected TableDescription getTable(CatalogDescription aCatalog,
                                            String aTableName) {
            String simpleName = unqualified(aTableName);
            TableDescription td = aCatalog.getTable(aTableName);
            if (td == null && !aTableName.equals(simpleName)) td = aCatalog.getTable(simpleName);
            if (td == null) {
                td = new TableDescription();
                td.setTableName(simpleName);
                if (aTableName.contains(".")) {
                    String[] array = aTableName.split("\\.");
                    if (array.length == 2) {
                        td.setCatalogName(array[0]);
                    }
                    if (array.length == 3) {  // ?? unclear what difference is between schema and catalog
                        td.setSchemaName(array[0]);
                        td.setCatalogName(array[1]);
                    }
                }
                aCatalog.addTable(td);
            }
            return td;
        }
    }

    class TableAddColumnsBuilder extends CatalogBuilder {
        // {table=customer, columndefinition=[{typeName=varchar,
        // column=ClientUserNumber, precision={numbers=[{value=37}]}}]}

        public void process(MapNode values, CatalogDescription catalog) {
            TableDescription td = getTable(catalog, values.getString("table"));
            List columns = values.getList("columndefinition");
            for (Object column : columns) {
                MapNode eachCol = new MapNode((Map) column);
                buildColumnDescription(eachCol, td);
            }
        }
    }

    class TableAlterColumnsBuilder extends CatalogBuilder {
        public void process(MapNode values, CatalogDescription catalog) {
            TableDescription td = getTable(catalog, values.getString("table"));
            List columns = values.getList("tableElement");
            for (Object column : columns) {
                Map map = (Map) column;
                if (map.containsKey("add-column")) { // add column
                    buildColumnDescription(new MapNode((Map) map.get("add-column")), td);
                } else if (map.containsKey("alter-column-type")) { // alter-column-type
                    MapNode node = new MapNode((Map) map.get("alter-column-type"));
                    String colName = strip(node.getString("column"));
                    ColumnDescription colDef = td.getColumn(colName);
                    setColType(node, colDef);
                } else if (map.containsKey("alter-column-drop-notnull")) { // alter-column-drop-notnull
                    MapNode node = new MapNode((Map) map.get("alter-column-drop-notnull"));
                    String colName = strip(node.getString("column"));
                    ColumnDescription colDef = td.getColumn(colName);
                    colDef.setNullable(true);
                } else if (map.containsKey("alter-column-set-notnull")) { // alter-column-set-notnull
                    MapNode node = new MapNode((Map) map.get("alter-column-set-notnull"));
                    String colName = strip(node.getString("column"));
                    ColumnDescription colDef = td.getColumn(colName);
                    colDef.setNullable(false);
                } else if (map.containsKey("drop-column")) {
                    String colName = strip(new MapNode((Map) map.get("add-column")).getString("column"));
                    td.removeColumn(colName);
                } else if (map.containsKey("drop-constraint")) {
                    String consName = strip(new MapNode((Map) map.get("drop-constraint")).getString("constraintName"));
                    td.removeConstraint(consName);
                } else if (map.containsKey("alter-column-drop-notnull")) {
                    MapNode node = new MapNode((Map) map.get("alter-column-drop-notnull"));
                    String colName = strip(node.getString("column"));
                    ColumnDescription colDef = td.getColumn(colName);
                    colDef.setNullable(true);
                }
            }
        }
    }

    class TableCommentBuilder extends CatalogBuilder {
        // {table=customer, columndefinition=[{typeName=varchar,
        // column=ClientUserNumber, precision={numbers=[{value=37}]}}]}

        public void process(MapNode values, CatalogDescription catalog) {
            TableDescription td = getTable(catalog, values.getString("table"));
            td.setComment((String) values.get("comment"));
        }
    }

    class ColumnCommentBuilder extends CatalogBuilder {
        // {table=customer, columndefinition=[{typeName=varchar,
        // column=ClientUserNumber, precision={numbers=[{value=37}]}}]}

        public void process(MapNode values, CatalogDescription catalog) {
            String qualifiedColumn = (String) values.get("tableColumn");
            String tableName = qualifiedColumn.substring(0, qualifiedColumn.indexOf('.'));
            String columnName = strip(qualifiedColumn.substring(tableName.length() + 1));
            TableDescription td = getTable(catalog, tableName);
            ColumnDescription col = td.getColumn(columnName);
            if (col == null) {
                col = new ColumnDescription();
                col.setColumnName(columnName);
                td.addColumn(col);
            }
            col.setComment((String) values.get("comment"));
        }
    }

    class TableAddConstraintBuilder extends CatalogBuilder {
        // {table=PHONENUMBER, constraint={tableSpace={tableSpace="DB_INDEX"}, unique=UNIQUE,
        // constraintName="PHONENUMBER_ATTR", columns=[{column="ATTROID"}, {column="TYPE"}]}}

        public void process(MapNode values, CatalogDescription catalog) {
            IndexDescription id = new IndexDescription();
            id.setTableName(strip(values.getString("table")));
            id.setTableSpace(strip(values.getString("constraint/tableSpace/tableSpace")));
            id.setIndexName(strip(values.getString("constraint/constraintName")));
            id.setUnique(getBool(values, "constraint/unique"));
            List columns = values.getList("constraint/columns");
            for (Object column : columns) {
                Map eachCol = (Map) column;
                id.addColumn(strip((String) eachCol.get("column")));
            }
            TableDescription td = getTable(catalog, id.getTableName());
            td.addConstraint(id);
        }
    }

    class CreateIndexBuilder extends CatalogBuilder {
        // {table=RENTALCARSTATION, tableSpace={tableSpace="DB_INDEX"}, unique=UNIQUE,
        // indexName=RENTALCARSTAT_IDX_CRS_STAT, columns=[{column="CRSTYPE"}, {column="STATIONID"}]}

        public void process(MapNode values, CatalogDescription catalog) {
            IndexDescription id = new IndexDescription();
            id.setTableName(strip(values.getString("table")));
            id.setTableSpace(strip(values.getString("tableSpace/tableSpace")));
            id.setIndexName(strip(values.getString("indexName")));
            id.setUnique(getBool(values, "unique"));
            List columns = values.getList("columns");
            for (Object column : columns) {
                Map eachCol = (Map) column;
                id.addColumn(strip((String) eachCol.get("column")));
                if (!id.isFunctionBased()) {
                    id.setFunctionBased(eachCol.get("func") != null);
                }
                // todo [RSt] missing build from "func"
            }
            getTable(catalog, id.getTableName()).addIndex(id);
        }
    }

    class TableAddForeignKeyBuilder extends CatalogBuilder {
        // {table=Customer, constraint={refcolumns=[{column="OBJECTIDENTIFIER"}],
        // refTable=CLIENTORGUNIT, constraintName="Customer_Company", columns=[{column="COMPANYID"}]}}

        public void process(MapNode values, CatalogDescription catalog) {
            ForeignKeyDescription fk = new ForeignKeyDescription();
            fk.setTableName(strip(values.getString("table")));
            fk.setConstraintName(strip(values.getString("constraint/constraintName")));
            fk.setRefTableName(strip(values.getString("constraint/refTable")));
            fk.setTableSpace(strip(values.getString("tableSpace/tableSpace")));
            fk.setOnDeleteRule(values.getString("constraint/onDeleteRule"));
            List columns = values.getList("constraint/columns");
            List refcolumns = values.getList("constraint/refcolumns/refcolumns");
            for (int i = 0; i < columns.size(); i++) {
                Map eachCol = (Map) columns.get(i);
                Map refCol = (refcolumns != null) ? (Map) refcolumns.get(i) : null;
                fk.addColumnPair(strip((String) eachCol.get("column")),
                        refCol != null ? strip((String) refCol.get("column")) : null);
            }
            TableDescription td = getTable(catalog, fk.getTableName());
            td.addForeignKey(fk);
        }
    }

    class DropSequenceBuilder extends CatalogBuilder {

        public void process(MapNode values, CatalogDescription catalog) throws IOException, TemplateException {
            String seqName = values.getString("sequence");
            catalog.removeSequence(seqName);
        }
    }

    class CreateSequenceBuilder extends CatalogBuilder {
        // {cache={value=100}, nominvalue=NOMINVALUE, increment=1, start=1,
        // noorder=NOORDER, nocycle=NOCYCLE, sequence=SEQ_NLSBundle, nomaxvalue=NOMAXVALUE}

        public void process(MapNode values, CatalogDescription catalog) {
            SequenceDescription sd = new SequenceDescription();
            sd.setSequenceName(strip(values.getString("sequence")));
            sd.setCache(getInt(values, "attributes/cache/value"));
            sd.setCycle(!getBool(values, "attributes/nocycle"));
            sd.setIncrement(getInt(values, "attributes/increment"));
            if (sd.getIncrement() == 0) sd.setIncrement(1);
            sd.setStart(getInt(values, "attributes/start"));
            if (sd.getStart() == 0) sd.setStart(1);
            //sd.setMaxValue();
            //sd.setMinValue();
            sd.setOrder(!getBool(values, "attributes/noorder"));
            catalog.addSequence(sd);
        }
    }

    class DropTableBuilder extends CatalogBuilder {
        public void process(MapNode values, CatalogDescription catalog) throws IOException, TemplateException {
            final String tableName = strip(values.getString("table"));
            catalog.removeTable(tableName);
        }
    }

    class CreateTableBuilder extends CatalogBuilder {
        // {table=NLSBUNDLE, columndefinition=[
        // {typeName=VARCHAR, column=DOMAIN, mandatory=NOT NULL, precision={numbers=[{value=500}]}}]}

        public void process(MapNode values, CatalogDescription catalog) throws IOException,
                TemplateException {
            final String tableName = strip(values.getString("table"));
            final TableDescription td = getTable(catalog, tableName);
            final List elements = values.getList("tableElement");
            for (Object element : elements) {
                MapNode theColDef = new MapNode((Map) element);
                if (theColDef.getMap().containsKey("columndefinition")) {
                    buildColumnDescription(
                            new MapNode((Map) theColDef.get("columndefinition")), td);
                } else if (theColDef.getMap().containsKey("primaryKey")) {
                    buildPrimaryKey(theColDef, td);
                } else if (theColDef.getMap().containsKey("foreignKey")) {
                    buildForeignKey(theColDef, td);
                }
            }
        }

        protected void buildForeignKey(MapNode aColDef, TableDescription aTd) {
            ForeignKeyDescription fk = new ForeignKeyDescription();
            fk.setTableName(aTd.getTableName());
            fk.setConstraintName(strip(aColDef.getString("foreignKey/constraint/constraintName")));
            fk.setRefTableName(strip(aColDef.getString("foreignKey/refTable")));
            fk.setTableSpace(
                    strip(aColDef.getString("foreignKey/tableSpace/tableSpace")));
            fk.setOnDeleteRule(aColDef.getString("foreignKey/onDeleteRule"));
            List columns = aColDef.getList("foreignKey/columns");
            List refcolumns = aColDef.getList("foreignKey/refcolumns/refcolumns");
            for (int j = 0; j < columns.size(); j++) {
                Map eachCol = (Map) columns.get(j);
                Map refCol = (refcolumns != null) ? (Map) refcolumns.get(j) : null;
                fk.addColumnPair(strip((String) eachCol.get("column")),
                        refCol != null ? strip((String) refCol.get("column")) : null);
            }
            aTd.addForeignKey(fk);
        }

        protected void buildPrimaryKey(MapNode aColDef, TableDescription aTd) {
            IndexDescription pk = new IndexDescription();
            pk.setTableName(aTd.getTableName());
            pk.setTableSpace(
                    strip(aColDef.getString("primaryKey/tableSpace/tableSpace")));
            pk.setIndexName(strip(aColDef.getString("primaryKey/constraint/constraintName")));
            pk.setUnique(true);
            List columns = aColDef.getList("primaryKey/columns");
            for (Object column : columns) {
                Map eachCol = (Map) column;
                pk.addColumn(strip((String) eachCol.get("column")));
            }
            aTd.setPrimaryKey(pk);
        }
    }

    // using syntax preferred by the DeZign ER Tool - not only there, but as a full table description creator

    protected class DezignCreateTableBuilder extends CreateTableBuilder {
        // {table=NLSBUNDLE, columndefinition=[
        // {typeName=VARCHAR, column=DOMAIN, mandatory=NOT NULL, precision={numbers=[{value=500}]}}]}

        public void process(MapNode values, CatalogDescription catalog) throws IOException,
                TemplateException {
            super.process(values, catalog);
            final String tableName = strip(values.getString("table"));
            final TableDescription td = getTable(catalog, tableName);
            final List elements = values.getList("tableElement");
            for (Object element : elements) {
                MapNode theColDef = new MapNode((Map) element);
                if (theColDef.getMap().containsKey("tableConstraint")) {
                    buildTableConstraint(theColDef, td);
                }
                if (theColDef.getMap().containsKey("tableIndex")) {
                    buildTableIndex(theColDef, td);
                }
                if (theColDef.getString("columndefinition/isUnique") != null) {
                    // unique column
                    IndexDescription index = new IndexDescription();
                    index.setTableName(td.getTableName());
                    index.addColumn(strip(theColDef.getString("columndefinition/column")));
                    index.setUnique(true);
//                    index.setIndexName(td.getTableName() + "_" + index.getColumn(0) + "_key");  // default for Postgres
                    td.addIndex(index);
                }
            }
        }

        protected void buildTableConstraint(MapNode aColDef, TableDescription aTd) {
            IndexDescription index = new IndexDescription();
            index.setTableName(aTd.getTableName());
            index.setTableSpace(
                    strip(aColDef.getString("tableConstraint/tableSpace/tableSpace")));
            index.setIndexName(strip(aColDef.getString(
                    "tableConstraint/constraint/constraintName")));
            List columns = aColDef.getList("tableConstraint/columns");
            for (Object column : columns) {
                Map eachCol = (Map) column;
                index.addColumn(strip((String) eachCol.get("column")));
            }
            if (aColDef.getString("tableConstraint/isPK") != null) {
                index.setUnique(true);
                aTd.setPrimaryKey(index);
            } else {
                if (aColDef.getString("tableConstraint/isUnique") != null) {
                    index.setUnique(true);
                }
                aTd.addConstraint(index);
            }
        }

        protected void buildTableIndex(MapNode aColDef, TableDescription aTd) {
            IndexDescription index = new IndexDescription();
            index.setTableName(aTd.getTableName());
            index.setTableSpace(
                    strip(aColDef.getString("tableIndex/tableSpace/tableSpace")));
            index.setIndexName(strip(aColDef.getString(
                    "tableIndex/indexName")));
            if(index.getIndexName() == null) {
                index.setIndexName(strip(aColDef.getString(
                    "tableIndex/optional/indexName")));
            }
            List columns = aColDef.getList("tableIndex/columns");
            for (Object column : columns) {
                Map eachCol = (Map) column;
                index.addColumn(strip((String) eachCol.get("column")));
            }
            if (aColDef.getString("tableIndex/isPK") != null) {
                index.setUnique(true);
                aTd.setPrimaryKey(index);
            } else {
                if (aColDef.getString("tableIndex/isUnique") != null) {
                    index.setUnique(true);
                }
                aTd.addIndex(index);
            }
        }
    }

    class TableAddPrimaryKey extends CatalogBuilder {
        // {table=NLSTEXT, constraint={tableSpace="DB_INDEX", constraintName="NLSTEXT_PK",
        // columns=[{column=BUNDLEID}, {column=LOCALE}, {column=KEY}]}}

        public void process(MapNode values, CatalogDescription catalog) {
            TableDescription td = getTable(catalog, values.getString("table"));
            IndexDescription pk = new IndexDescription();
            pk.setTableName(strip(td.getTableName()));
            pk.setTableSpace(strip(values.getString("constraint/tableSpace/tableSpace")));
            pk.setIndexName(strip(values.getString("constraint/constraintName")));
            pk.setUnique(true);
            List columns = values.getList("constraint/columns");
            for (Object column : columns) {
                Map eachCol = (Map) column;
                pk.addColumn(strip((String) eachCol.get("column")));
            }
            td.setPrimaryKey(pk);
        }
    }

    public CatalogDescription getCatalog() {
        if (catalog == null) {
            setCatalog(new CatalogDescription());
        }
        return catalog;
    }

    public void setCatalog(CatalogDescription aCatalog) {
        catalog = aCatalog;
    }

    /**
     * API -
     * not thread-safe. only fill one catalog at the same time with this instance.
     *
     * @param scriptURL  - URL to a script to parse
     * @param fileFormat - SQL, JDBC, STMT supported
     * @throws java.io.IOException   - url not found
     * @throws java.sql.SQLException - error executing SQL
     */
    public void fillCatalog(URL scriptURL, ScriptAction.FileFormat fileFormat) throws SQLException, IOException {
        SQLScriptParser parser = new SQLScriptParser(log);
        if (fileFormat == null || fileFormat.equals(ScriptAction.FileFormat.SQL)) {
            parser.iterateSQLScript(this, scriptURL);
        } else if (fileFormat.equals(ScriptAction.FileFormat.JDBC)) {
            parser.iterateSQLLines(this, scriptURL);
        } else if (fileFormat.equals(ScriptAction.FileFormat.STMT)) {
            parser.execSQLScript(this, scriptURL);
        } else {
            throw new UnsupportedOperationException(scriptURL + "is not a supported file type: " + fileFormat);
        }
    }

    public void fillCatalog(URL scriptURL) throws SQLException, IOException {
        fillCatalog(scriptURL, null);
    }

    /**
     * parse the statement and create the adequate parts of the Catalog
     *
     * @param statement - a DDL statement (Oracle syntax)
     * @return 0
     * @throws SQLException
     */
    public int visitStatement(String statement) throws SQLException {
        int found = 0;
        for (ExtractExpr theExpr : ddlSpec.getExpressions()) {
            Map values = extractor.extract(statement, theExpr);
            if (values != null) {
                found++;
                if (log.isDebugEnabled()) {
                    log.debug("FOUND " + theExpr.getName() + " in: " + statement);
                    log.debug(String.valueOf(values));
                }
                CatalogBuilder builder = builders.get(theExpr.getName());
                if (builder != null) {
                    try {
                        builder.process(new MapNode(values), getCatalog());
                    } catch (Exception e) {
                        log.error("error processing " + values, e);
                    }
                }
                break; // we stop when the first expression matches. maybe we want to change this in the future...?
            }
        }
        if (found == 0) {
            if (log.isDebugEnabled()) log.debug("IGNORE: " + statement);
        }
        return 0;
    }

    public void visitComment(String theComment) throws SQLException {
        // intentionally empty, do nothing
    }

    public void doCommit() throws SQLException {
        // intentionally empty, do nothing
    }

    public void doRollback() throws SQLException {
        // intentionally empty, do nothing
    }
}