package com.agimatec.sql.meta.script;

import com.agimatec.commons.config.MapNode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public class PropertiesExtractorTest extends TestCase {

    public PropertiesExtractorTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(PropertiesExtractorTest.class);
    }

    public void testExtractExprBuilder() throws ParseException {
        String input = "{create-table CREATE TABLE ${table} '(' " +
                "{columndefinition ${column} ${typeName} " +
                "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]...','} ')'}";
        ExtractExpr expr = ExtractExprBuilder.buildExpr(input);
        assertNotNull(expr);
        assertEquals(input, expr.toString());
    }

    // CREATE TABLE ${table} (${@columndefinition {${column} ${typeName} ${@nullable [NULL|NOT NULL]}},...});
    //
    // ALTER TABLE ${table} ADD(CONSTRAINT ${constraintName} UNIQUE( ${column,...} USING INDEX TABLESPACE "${tableSpace}");
    // ALTER TABLE ${table} ADD(${columnname} ${type} {NULL|NOT NULL})
    // CREATE SEQUENCE ${sequenceName} INCREMENT BY ${increment}
    //   START WITH ${*} ${NOMAXVALUE} ${NOMINVALUE [NOMINVALUE]} ${NOCYCLE [NOCYCLE]} ${NOORDER [NOORDER]} CACHE ${cache};
    // CREATE ${unique [UNIQUE]} INDEX ${indexName} ON ${tableName} (  ${@columnorder {${column} ${order}},...}) TABLESPACE DB_INDEX;
    public void testCREATE_TABLE_1() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("CREATE").addWord("TABLE").addProperty("table");
        expr.addSeparator("(").addExpr(
                new ExtractExpr("columndefinition").addProperty("column").addProperty(
                        "typeName")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' {columndefinition ${column} ${typeName}} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "CREATE TABLE Customer (DateBirth DATE)";
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        Map columndefinition = (Map) values.get("columndefinition");
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
    }

    public void testCREATE_TABLE_2() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                        "[{primaryKey PRIMARY KEY '(' {columns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                        "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{columndefinition ${column} ${typeName} " +
                        "[{precision '(' {numbers ${value}...','} [CHAR]')'}] [${mandatory(NOT NULL)}]}] " +
                        "...','} ')'}");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "create table JBPM_COMMENT (ID_ number(19,0) not null, VERSION_ number(10,0) not null," +
                        "ACTORID_ varchar2(255 char), TIME_ timestamp, MESSAGE_ varchar2(4000 char)," +
                        "TOKEN_ number(19,0), TASKINSTANCE_ number(19,0), TOKENINDEX_ number(10,0), TASKINSTANCEINDEX_ number(10,0)," +
                        "primary key (ID_))";
        Map values = extract.extract(sql, expr);
        assertNotNull(values);
        MapNode mn = new MapNode(values);
        assertEquals("ACTORID_", mn.getNode("tableElement/2/columndefinition/column"));
        assertEquals("TIME_", mn.getNode("tableElement/3/columndefinition/column"));
        assertEquals("255",
                mn.getNode("tableElement/2/columndefinition/precision/numbers/0/value"));
    }

    public void testParseSequence() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-sequence");
        expr.addWord("CREATE").addWord("SEQUENCE").addProperty("sequenceName")
                .addWord("INCREMENT").addWord("BY").addProperty("increment")
                .addWord("START").addWord("WITH").addProperty("start");
        expr = ExtractExprBuilder.buildExpr(
                "{create-sequence CREATE SEQUENCE ${sequenceName} INCREMENT BY ${increment} START WITH ${start}}");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "CREATE sequence seq_sample increment by 10 start with 1";
        Map values = extract.extract(sql, expr);
        assertEquals("seq_sample", values.get("sequenceName"));
        assertEquals("10", values.get("increment"));
        assertEquals("1", values.get("start"));
    }

    public void testCREATE_TABLE_1_SepInside() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("CREATE").addWord("TABLE").addProperty("table");
        expr.addExpr(new ExtractExpr("columndefinition").addSeparator("(")
                .addProperty("column").addProperty("typeName").addSeparator(")"));
        expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} {columndefinition '(' ${column} ${typeName} ')'}}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "CREATE TABLE Customer (DateBirth DATE)";
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        Map columndefinition = (Map) values.get("columndefinition");
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
    }

    public void testCREATE_TABLE_3() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                        "[{primaryKey PRIMARY KEY '(' {columns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                        "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{columndefinition ${column} ${typeName} [${varying(VARYING)}]" +
                        "[{precision '(' {numbers ${value}...','} [CHAR]')'}] " +
                        "[{constraint CONSTRAINT ${constraintName}}] " +
                        "[${mandatory(NOT NULL)}]}] " + "...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "CREATE TABLE Address_0 (field_1 CHARACTER VARYING(255) CONSTRAINT NN_address_id NOT NULL, field_2 CHARACTER VARYING(255) )";
        Map values = extract.extract(sql, expr);
        assertEquals("Address_0", values.get("table"));
        List tableElements = (List) values.get("tableElement");
        assertEquals("field_1",
                ((Map) ((Map) tableElements.get(0)).get("columndefinition")).get(
                        "column"));
        assertEquals("field_2",
                ((Map) ((Map) tableElements.get(1)).get("columndefinition")).get(
                        "column"));
    }

    public void testCREATE_TABLE_4() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                        "[{primaryKey PRIMARY KEY '(' {columns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                        "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{columndefinition ${column} ${typeName} [${varying(VARYING)}]" +
                        "[{precision '(' {numbers ${value}...','} [CHAR]')'}] " +
                        "[{constraint CONSTRAINT ${constraintName}}] " +
                        "[${mandatory(NOT NULL)}]}] " + "...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "CREATE TABLE Address_2 (address_id BIGINT CONSTRAINT NN_address_id NOT NULL)";
        Map values = extract.extract(sql, expr);
        assertEquals("Address_2", values.get("table"));
        List tableElements = (List) values.get("tableElement");
        assertEquals("address_id",
                ((Map) ((Map) tableElements.get(0)).get("columndefinition")).get(
                        "column"));
    }

    public void testCREATE_TABLE_5() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                        "[{tableConstraint [{constraint CONSTRAINT ${constraintName}}] [${isPK(PRIMARY KEY)}] [${isUnique(UNIQUE)}] '(' {columns ${column}...','} ')'] " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                        "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{columndefinition ${column} ${typeName} [${varying(VARYING)}]" +
                        "[{precision '(' {numbers ${value}...','} [CHAR]')'}] " +
                        "[{constraint CONSTRAINT ${constraintName}}] " +
                        "[${mandatory(NOT NULL)}]}] " + "...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "CREATE TABLE User_Core (" +
                "    user_id BIGINT CONSTRAINT NN_user_id NOT NULL," +
                "    email CHARACTER VARYING(50)," +
                "    mobile_prefix CHARACTER VARYING(40)," +
                "    mobile_number CHARACTER VARYING(40)," +
                "    state BIGINT CONSTRAINT NN_state NOT NULL," +
                "    role_id BIGINT CONSTRAINT NN_role_id NOT NULL," +
                "    address_id BIGINT CONSTRAINT NN_address_id NOT NULL," +
                "    first_name CHARACTER VARYING(40) CONSTRAINT NN_first_name NOT NULL," +
                "    last_name CHARACTER VARYING(40) CONSTRAINT NN_last_name NOT NULL," +
                "    user_identification CHARACTER VARYING(40) CONSTRAINT NN_user_identification NOT NULL," +
                "    registration_time TIMESTAMP CONSTRAINT NN_registration_time NOT NULL," +
                "    type SMALLINT," + "    gender CHARACTER VARYING(10)," +
                "    locale_code CHARACTER VARYING(10)," +
                "    CONSTRAINT User_Core_pkey PRIMARY KEY (user_id)," +
                "    CONSTRAINT TUC_User_Core_1 UNIQUE (user_identification)" + ")";
        Map values = extract.extract(sql, expr);
        assertEquals("User_Core", values.get("table"));
        List tableElements = (List) values.get("tableElement");
        assertEquals("TUC_User_Core_1",
                ((Map) ((Map) ((Map) tableElements.get(15)).get("tableConstraint"))
                        .get("constraint")).get("constraintName"));
        assertEquals("User_Core_pkey",
                ((Map) ((Map) ((Map) tableElements.get(14)).get("tableConstraint"))
                        .get("constraint")).get("constraintName"));
        assertEquals("UNIQUE",
                ((Map) ((Map) tableElements.get(15)).get("tableConstraint")).get(
                        "isUnique"));
        assertEquals("PRIMARY KEY",
                ((Map) ((Map) tableElements.get(14)).get("tableConstraint")).get("isPK"));
    }

    public void testCREATE_TABLE_NOT_NULL() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("CREATE").addWord("TABLE").addProperty("table");
        expr.addSeparator("(").addExpr(new ExtractExpr("columndefinition")
                .addProperty("column").addProperty("typeName").addOptionalProperty(
                "NOT NULL", "mandatory")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' {columndefinition ${column} ${typeName} [${mandatory(NOT NULL)}]} ')'}");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "CREATE TABLE Customer (DateBirth DATE NOT NULL)";
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        Map columndefinition = (Map) values.get("columndefinition");
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
        assertEquals("NOT NULL", columndefinition.get("mandatory"));
    }

    public void testTableComment() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder
                .buildExpr("{table-comment COMMENT ON TABLE ${table} IS ${comment{'}}}");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "COMMENT ON TABLE User_Core IS 'Speichert die User-Daten '";
        Map values = extract.extract(sql, expr);
        assertEquals("User_Core", values.get("table"));
        assertEquals("Speichert die User-Daten ", values.get("comment"));

        sql = "COMMENT ON TABLE User_Core IS '(BarCode)Type'";
        values = extract.extract(sql, expr);
        assertEquals("(BarCode)Type", values.get("comment"));
    }

    public void testWordWithDelimeters() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{html GRAB THIS [${starttag(body){<}{>}}] [${endtag(/body){<}{>}}]}");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "GRAB THIS <body>";
        Map values = extract.extract(sql, expr);
        assertEquals("body", values.get("starttag"));

        sql = "GRAB THIS </body>";
        values = extract.extract(sql, expr);
        assertEquals("/body", values.get("endtag"));

        sql = "GRAB THIS body lotion";
        values = extract.extract(sql, expr);
        assertTrue(values.isEmpty());

        expr = ExtractExprBuilder
                .buildExpr("{html Buy the [${tag(body lotion){<}{/>}}]}");

        sql = "Buy the <Body Lotion/> in the store.";
        values = extract.extract(sql, expr);
        assertEquals("Body Lotion", values.get("tag"));
    }

    public void testColumnComment() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{column-comment COMMENT ON COLUMN ${tableColumn} IS ${comment{'}}}");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "COMMENT ON COLUMN User_Core.gender IS 'MALE or FEMALE or null as GenderEnum'";
        Map values = extract.extract(sql, expr);
        assertEquals("User_Core.gender", values.get("tableColumn"));
        assertEquals("MALE or FEMALE or null as GenderEnum", values.get("comment"));
    }

    public void testCREATE_TABLE_NOT_NULL_2cols() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("CREATE").addWord("TABLE").addProperty("table");
        expr.addSeparator("(").addExpr(new ExtractExpr("columndefinition", ",")
                .addProperty("column").addProperty("typeName").addOptionalProperty(
                "NOT NULL", "mandatory")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' {columndefinition ${column} ${typeName} [${mandatory(NOT NULL)}]...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "CREATE TABLE Customer (DateBirth DATE NOT NULL , FirstName VARCHAR NULL)";
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        List columndefinitions = (List) values.get("columndefinition");
        Map columndefinition = (Map) columndefinitions.get(0);
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
        assertEquals("NOT NULL", columndefinition.get("mandatory"));

        columndefinition = (Map) columndefinitions.get(1);
        assertEquals("FirstName", columndefinition.get("column"));
        assertEquals("VARCHAR", columndefinition.get("typeName"));
        assertEquals(null, columndefinition.get("mandatory"));

        //
        sql =
                "CREATE TABLE Customer (DateBirth DATE NULL,FirstName VARCHAR NULL, Lastname CHAR NOT NULL)";
        values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        columndefinitions = (List) values.get("columndefinition");
        columndefinition = (Map) columndefinitions.get(0);
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
        assertEquals(null, columndefinition.get("mandatory"));

        columndefinition = (Map) columndefinitions.get(1);
        assertEquals("FirstName", columndefinition.get("column"));
        assertEquals("VARCHAR", columndefinition.get("typeName"));
        assertEquals(null, columndefinition.get("mandatory"));

        columndefinition = (Map) columndefinitions.get(2);
        assertEquals("Lastname", columndefinition.get("column"));
        assertEquals("CHAR", columndefinition.get("typeName"));
        assertEquals("NOT NULL", columndefinition.get("mandatory"));
    }

    public void testCREATE_Cols_With_Length() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("CREATE").addWord("TABLE").addProperty("table");
        expr.addSeparator("(").addExpr(new ExtractExpr("columndefinition", ",")
                .addProperty("column").addProperty("typeName")
                .addOptionalExpr(new ExtractExpr("precision").addSeparator("(")
                        .addProperty("length").addSeparator(")")).addOptionalProperty(
                "NOT NULL", "mandatory")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' {columndefinition ${column} ${typeName} [{precision '(' ${length} ')'}] [${mandatory(NOT NULL)}]...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "CREATE TABLE Customer (DateBirth DATE NOT NULL,FirstName VARCHAR(100) NOT NULL , LastName VARCHAR (150) NULL)";
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        List columndefinitions = (List) values.get("columndefinition");

        Map columndefinition;

        columndefinition = (Map) columndefinitions.get(0);
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
        assertEquals(null, columndefinition.get("precision"));
        assertEquals("NOT NULL", columndefinition.get("mandatory"));

        columndefinition = (Map) columndefinitions.get(1);
        assertEquals("FirstName", columndefinition.get("column"));
        assertEquals("VARCHAR", columndefinition.get("typeName"));
        Map precision = (Map) columndefinition.get("precision");
        assertEquals("100", precision.get("length"));
        assertEquals("NOT NULL", columndefinition.get("mandatory"));

        columndefinition = (Map) columndefinitions.get(2);
        assertEquals("LastName", columndefinition.get("column"));
        assertEquals("VARCHAR", columndefinition.get("typeName"));
        precision = (Map) columndefinition.get("precision");
        assertEquals("150", precision.get("length"));
        assertEquals(null, columndefinition.get("mandatory"));
    }

    public void testCREATENumber_with_Precision() throws ParseException {
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("CREATE").addWord("TABLE").addProperty("table");
        expr.addSeparator("(").addExpr(new ExtractExpr("columndefinition", ",")
                .addProperty("column").addProperty("typeName").addOptionalExpr(
                new ExtractExpr("precision").addSeparator("(").addExpr(
                        new ExtractExpr("numbers", ",").addProperty(
                                "value")).addSeparator(")")).addOptionalProperty(
                "NOT NULL", "mandatory")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' {columndefinition ${column} ${typeName} [{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "CREATE TABLE Rate (PRICE NUMBER(9,2) NOT NULL, PRICE2 NUMBER(2), PRICE3 INTEGER, PRICE4 CHAR)";
        Map values = extract.extract(sql, expr);
        assertEquals("Rate", values.get("table"));
        List columns = (List) values.get("columndefinition");

        Map column;
        column = (Map) columns.get(0);
        assertEquals("PRICE", column.get("column"));
        assertEquals("NUMBER", column.get("typeName"));
        Map precision = (Map) column.get("precision");
        List numbers = (List) precision.get("numbers");
        assertEquals("9", ((Map) numbers.get(0)).get("value"));
        assertEquals("2", ((Map) numbers.get(1)).get("value"));
        assertEquals(2, numbers.size());

        column = (Map) columns.get(1);
        assertEquals("PRICE2", column.get("column"));
        assertEquals("NUMBER", column.get("typeName"));
        precision = (Map) column.get("precision");
        numbers = (List) precision.get("numbers");
        assertEquals("2", ((Map) numbers.get(0)).get("value"));
        assertEquals(1, numbers.size());

        column = (Map) columns.get(2);
        assertEquals("PRICE3", column.get("column"));
        assertEquals("INTEGER", column.get("typeName"));
        assertEquals(null, column.get("precision"));
    }

    public void testALTER_TABLE() throws ParseException {
        // ALTER TABLE ${table} ADD(CONSTRAINT ${constraintName} UNIQUE( ${column,...})  USING INDEX TABLESPACE ${tableSpace})
        ExtractExpr expr = new ExtractExpr("alter-table");
        expr.addWord("ALTER").addWord("TABLE").addProperty("table");
        expr.addWord("ADD");
        expr.addSeparator("(").addExpr(new ExtractExpr("constraint").addWord("CONSTRAINT")
                .addProperty("constraintName").addOptionalProperty("UNIQUE", "unique")
                .addSeparator("(")
                .addExpr(new ExtractExpr("columns", ",").addProperty("column"))
                .addSeparator(")").addWord("USING").addWord("INDEX")
                .addWord("TABLESPACE").addProperty("tableSpace")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{alter-table ALTER TABLE ${table} ADD '(' {constraint CONSTRAINT ${constraintName} [${unique(UNIQUE)}] '(' {columns ${column}...','} ')' USING INDEX TABLESPACE ${tableSpace}} ')'}");

        String sql =
                "ALTER TABLE Customer ADD(CONSTRAINT Cust_name UNIQUE( firstname, lastname) USING INDEX TABLESPACE SAMPLE_IDX)";
        PropertiesExtractor extract = new PropertiesExtractor();
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        Map constraint = (Map) values.get("constraint");
        assertEquals("Cust_name", constraint.get("constraintName"));
        List columns = (List) constraint.get("columns");
        assertEquals("firstname", ((Map) columns.get(0)).get("column"));
        assertEquals("lastname", ((Map) columns.get(1)).get("column"));
        assertEquals("SAMPLE_IDX", constraint.get("tableSpace"));
    }

    public void testALTER_TABLE2() throws ParseException {
        // ALTER TABLE ${table} ADD(${column} ${type} ${nullable});
        ExtractExpr expr = new ExtractExpr("create-table");
        expr.addWord("ALTER").addWord("TABLE").addProperty("table").addWord("ADD");
        expr.addSeparator("(").addExpr(new ExtractExpr("columndefinition", ",")
                .addProperty("column").addProperty("typeName").addOptionalProperty(
                "NOT NULL", "mandatory")).addSeparator(")");

        expr = ExtractExprBuilder.buildExpr(
                "{create-table ALTER TABLE ${table} ADD '(' {columndefinition ${column} ${typeName} [${mandatory(NOT NULL)}]...','} ')'}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql = "ALTER TABLE Customer ADD(DateBirth DATE NOT NULL)";
        Map values = extract.extract(sql, expr);
        assertEquals("Customer", values.get("table"));
        List columndefinitions = (List) values.get("columndefinition");
        Map columndefinition = (Map) columndefinitions.get(0);
        assertEquals("DateBirth", columndefinition.get("column"));
        assertEquals("DATE", columndefinition.get("typeName"));
        assertEquals("NOT NULL", columndefinition.get("mandatory"));
    }

    public void testCreateIndexNoTableSpace() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-index CREATE [${unique(UNIQUE)}] INDEX ${indexName} ON ${table} '(' {columns ${column} [ASC] [${desc(DESC)}]...','} ')' " +
                        "[{tableSpace TABLESPACE ${tableSpace}}] }");
        String sql =
                "CREATE INDEX EXCHANGERATE_STARTPERIOD    ON EXCHANGERATE(STARTPERIOD ASC, ENDPERIOD, MIDDLE, SAMPLE DESC)";
        PropertiesExtractor extract = new PropertiesExtractor();
        Map values = extract.extract(sql, expr);
        assertTrue(null != values);
        List columns = (List) values.get("columns");
        Map column = (Map) columns.get(0);
        assertEquals("STARTPERIOD", column.get("column"));

        column = (Map) columns.get(1);
        assertEquals("ENDPERIOD", column.get("column"));

        column = (Map) columns.get(2);
        assertEquals("MIDDLE", column.get("column"));

        column = (Map) columns.get(3);
        assertEquals("SAMPLE", column.get("column"));
        assertEquals("DESC", column.get("desc"));
    }

    // function based not completely supported ! (this is sufficient in most cases)
    public void testFunctionBasedIndex() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-index CREATE [${unique(UNIQUE)}] INDEX ${indexName} ON ${table} '(' {columns ${column} [{func '(' {elements ${each}...','} ')'}] [ASC] [${desc(DESC)}]...','} ')' " +
                        "[{tableSpace TABLESPACE ${tableSpace}}] }");
        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
                "CREATE INDEX HotelAddress_FI1 ON HotelAddress (lower(City), countrycode, bitand(city, 2)) TABLESPACE \"DB_INDEX\"";
        MapNode values = new MapNode(extract.extract(sql, expr));

        assertEquals("HotelAddress_FI1", values.get("indexName"));
        assertEquals("HotelAddress", values.get("table"));

        assertEquals("\"DB_INDEX\"", values.getString("tableSpace/tableSpace"));

        assertEquals("lower", values.getString("columns/0/column"));
        assertEquals("City", values.getString("columns/0/func/elements/0/each"));

        assertEquals("bitand", values.getString("columns/2/column"));
        assertEquals("city", values.getString("columns/2/func/elements/0/each"));
        assertEquals("2", values.getString("columns/2/func/elements/1/each"));
    }

    public void testForeignKey() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{table-add-foreign-key ALTER TABLE ${table} ADD '(' " +
                        "{constraint CONSTRAINT ${constraintName} FOREIGN KEY '(' {columns ${column}...','} ')' " +
                        "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace}]}] ')'}");

        String sql = "ALTER TABLE Customer ADD (CONSTRAINT \"Customer_Company\" " +
                "FOREIGN KEY (\"COMPANYID\") REFERENCES CLIENTORGUNIT (\"OBJECTIDENTIFIER\"))";
        PropertiesExtractor extract = new PropertiesExtractor();
        MapNode values = new MapNode(extract.extract(sql, expr));
        assertEquals("\"Customer_Company\"",
                values.getString("constraint/constraintName"));
        assertEquals("CLIENTORGUNIT", values.getString("constraint/refTable"));
        assertEquals("\"COMPANYID\"", values.getString("constraint/columns/0/column"));
        assertEquals("\"OBJECTIDENTIFIER\"",
                values.getString("constraint/refcolumns/0/column"));
    }

    public void testCreateTableWithPrimaryKeyAndForeignKey() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{create-table CREATE TABLE ${table} '(' " + "{tableElement " +
                        "[{primaryKey PRIMARY KEY '(' {columns ${column}...','} ')' }]" +
                        "[{foreignKey FOREIGN KEY '(' {columns ${column}...','} ')' " +
                        "REFERENCES ${refTable} '(' {refcolumns ${column}...','} ')' " +
                        "[{tableSpace USING INDEX TABLESPACE ${tableSpace} }] }]" +
                        "[{columndefinition ${column} ${typeName} " +
                        "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]}] " +
                        "...','} ')'}");
        String sql =
                "create table testtable (col1 varchar(100), col2 number(9,2) NOT NULL, " +
                        "PRIMARY KEY(ObjectIdentifier, ObjectVersion)," +
                        " FOREIGN KEY (SAMPLE_ID) REFERENCES VOUCHER_SAMPLE(OID))";
        PropertiesExtractor extract = new PropertiesExtractor();
        MapNode values = new MapNode(extract.extract(sql, expr));
        assertNotNull(values);
        assertEquals("testtable", values.getString("table"));

        assertEquals("col1", values.getString("tableElement/0/columndefinition/column"));
        assertEquals("col2", values.getString("tableElement/1/columndefinition/column"));
        assertEquals("ObjectIdentifier",
                values.getString("tableElement/2/primaryKey/columns/0/column"));
        assertEquals("ObjectVersion",
                values.getString("tableElement/2/primaryKey/columns/1/column"));
        assertEquals("VOUCHER_SAMPLE",
                values.getString("tableElement/3/foreignKey/refTable"));
        assertEquals("SAMPLE_ID",
                values.getString("tableElement/3/foreignKey/columns/0/column"));

    }

}
