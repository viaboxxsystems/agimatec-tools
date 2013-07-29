package com.agimatec.sql.meta.mysql;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.sql.meta.ColumnDescription;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * <p></p>
 * User: roman.stumm@viaboxx.de<br>
 * Date: 26.03.13
 */
public class MySqlDDLExpressionsTest {
    DDLScriptSqlMetaFactory factory;

    @Before
    public  void before() {
        factory = new DDLScriptSqlMetaFactory(DDLExpressions.forDbms("mysql"));
    }

    @Test
    public void split() {
        String[] a = "mycat.my_db.mytable".split("\\.");
        assertEquals("mycat", a[0]);
        assertEquals("my_db", a[1]);
        assertEquals("mytable", a[2]);
    }

    @Test
    public void test() throws IOException, SQLException {
        URL script = ConfigManager.toURL("cp://mysql/mysql-schema.sql");
        factory.fillCatalog(script);
        assertNotNull(factory.getCatalog());
        assertEquals(8, factory.getCatalog().getTablesSize());
        assertEquals(0, factory.getCatalog().getSequencesSize());
        assertEquals("OTHER_TABLE", factory.getCatalog().getTable("OTHER_TABLE").getTableName());
        assertEquals("TEST_TABLE", factory.getCatalog().getTable("TEST_TABLE").getTableName());
        assertEquals("other_db", factory.getCatalog().getTable("TEST_TABLE").getCatalogName());
        assertEquals("other_table_seq", factory.getCatalog().getTable("OTHER_TABLE_SEQ").getTableName());

        TableDescription table = factory.getCatalog().getTable("TEST_TABLE");
        assertEquals(2, table.getPrimaryKey().getColumnSize());
        assertEquals("client_id", table.getPrimaryKey().getColumn(0));
        assertEquals("version", table.getPrimaryKey().getColumn(1));

        table = factory.getCatalog().getTable("OTHER_TABLE");
        assertEquals(1, table.getPrimaryKey().getColumnSize());
        assertEquals("JOB_INSTANCE_ID", table.getPrimaryKey().getColumn(0));
        assertNotNull(table.getForeignKey("OTHER_TABLE_FK"));

        table = factory.getCatalog().getTable("http_resource_directory");
        assertNotNull(table);
        assertNotNull(table.getColumn("nid"));
        assertNotNull(table.getColumn("ncrc32"));
        assertEquals("int unsigned", table.getColumn("ncrc32").getTypeName());
        assertNotNull(table.getColumn("surl"));
        assertNotNull(table.getColumn("scharacterset"));
        assertNotNull(table.getColumn("jj"));
        assertEquals(1, table.getIndex("IDX_CRC").getColumnSize());
        assertEquals(1, table.getIndex("IDX_PURGE").getColumnSize());

        table = factory.getCatalog().getTable("config");
        assertNotNull(table);
        assertEquals(1, table.getIndexSize());
        assertNotNull(table.getIndex("skey"));
        assertEquals(3, table.getIndex("skey").getColumnSize());
        assertTrue(table.getIndex("skey").isUnique());

        table = factory.getCatalog().getTable("commented_table");
        assertNotNull(table);
        assertEquals("value can be 1-3", table.getColumn("commented_column").getComment());

        table = factory.getCatalog().getTable("maps_session_attributes");
        assertNotNull(table);

        table = factory.getCatalog().getTable("maps_service");
        assertNotNull(table);
    }

    @Test
    @Ignore
    public void test2() throws IOException, SQLException {
        URL script = ConfigManager.toURL("cp://mysql/mysql-real-schema.sql");
        factory.fillCatalog(script);
        assertNotNull(factory.getCatalog());
        assertEquals(10, factory.getCatalog().getTablesSize());
        assertEquals(0, factory.getCatalog().getSequencesSize());

        TableDescription table = factory.getCatalog().getTable("imp_prefetch");
        assertNotNull(table);
        ColumnDescription column = table.getColumn("created_by");
        assertNotNull(column);
    }
}
