package com.agimatec.dbtransform;

import com.agimatec.commons.config.MapNode;
import com.agimatec.sql.meta.script.ExtractExpr;
import com.agimatec.sql.meta.script.ExtractExprBuilder;
import com.agimatec.sql.meta.script.PropertiesExtractor;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.util.Map;

/**
 * ScriptTransformator Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>12/17/2007</pre>
 */
public class ScriptTransformatorTest extends TestCase {
    public ScriptTransformatorTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * parse {add column ...} and {alter column ... type ...}
     *
     * @throws ParseException
     */
    public void testTableAlterColumns() throws ParseException {
        ExtractExpr expr = ExtractExprBuilder.buildExpr(
                "{table-alter-columns ALTER TABLE ${table} {tableElement" +
                        "[{alter-column-set-notnull ALTER [COLUMN] ${column} SET NOT NULL}]"+

                        "[{alter-column-drop-notnull ALTER [COLUMN] ${column} DROP NOT NULL}]"+

                        "[{add-column ADD [COLUMN] ${column} ${typeName} [${varying(VARYING)}]" +
                        "[{default DEFAULT ${defaultValue}}] " +
                        "[{constraint CONSTRAINT ${constraintName}}] " +
                        "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]}] " +

                        "[{alter-column-type ALTER [COLUMN] ${column} TYPE ${typeName} [${varying(VARYING)}]" +
                        "[{default DEFAULT ${defaultValue}}] " +
                        "[{constraint CONSTRAINT ${constraintName}}] " +
                        "[{precision '(' {numbers ${value}...','} ')'}] [${mandatory(NOT NULL)}]}]" +

                        "...','}}");

        PropertiesExtractor extract = new PropertiesExtractor();
        String sql =
              "ALTER TABLE test alter column col2 TYPE varchar(200), " +
                        "alter column col3 type varchar(150), " +
                        "add col4 integer not null," +
                        "alter column col5 drop not null," +
                        "alter col6 set not null";
        Map values = extract.extract(sql, expr);
        MapNode node = new MapNode(values);
        assertEquals("test", values.get("table"));
        assertEquals("col2", node.getNode("tableElement/0/alter-column-type/column"));
        assertEquals("col3", node.getNode("tableElement/1/alter-column-type/column"));
        assertEquals("col4", node.getNode("tableElement/2/add-column/column"));
        assertEquals("not null", node.getNode("tableElement/2/add-column/mandatory"));
        assertEquals("col5", node.getNode("tableElement/3/alter-column-drop-notnull/column"));
        assertEquals("col6", node.getNode("tableElement/4/alter-column-set-notnull/column"));

        sql = "ALTER TABLE Address ADD COLUMN version INTEGER DEFAULT 0 CONSTRAINT NN_version NOT NULL";
        values = extract.extract(sql, expr);
        node = new MapNode(values);
        assertEquals("Address", node.getString("table"));
        assertEquals("version", node.getString("tableElement/0/add-column/column"));
    }

    public static Test suite() {
        return new TestSuite(ScriptTransformatorTest.class);
    }
}
