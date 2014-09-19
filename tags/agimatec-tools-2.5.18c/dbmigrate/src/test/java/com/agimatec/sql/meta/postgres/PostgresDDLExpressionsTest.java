package com.agimatec.sql.meta.postgres;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.sql.meta.TableDescription;
import com.agimatec.sql.meta.persistence.ObjectPersistencer;
import com.agimatec.sql.meta.persistence.SerializerPersistencer;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.meta.script.DDLScriptSqlMetaFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Description: <br>
 * <p>
 * User: roman.stumm<br>
 * Date: 07.05.13<br>
 * Time: 10:47<br>
 * viaboxx GmbH, 2013
 * </p>
 */
public class PostgresDDLExpressionsTest {
    DDLScriptSqlMetaFactory factory;

    @Before
    public void before() {
        factory = new DDLScriptSqlMetaFactory(DDLExpressions.forDbms("postgres"));
    }

    @Test
    public void fillCatalogAndSave()
            throws Exception {
        URL script = ConfigManager.toURL("file:src/test/resources/create-tables-example-script.sql");
        factory.fillCatalog(script);
        assertNotNull(factory.getCatalog());

        File file;

        new File("target").mkdir();

        ObjectPersistencer persistencer = new SerializerPersistencer();
        file = new File("target/catalog-example.dmp");
        persistencer.save(factory.getCatalog(), file);

        persistencer = new XStreamPersistencer();
        file = new File("target/catalog-example.xml");
        persistencer.save(factory.getCatalog(), file);

    }

    @Test
    public void test() throws IOException, SQLException {
        URL script = ConfigManager.toURL("file:src/test/resources/create-tables-example-script.sql");
        factory.fillCatalog(script);
        TableDescription table = factory.getCatalog().getTable("alternative_foreignkey_syntax");
        assertNotNull(factory.getCatalog());
        assertEquals(19, factory.getCatalog().getTablesSize());
        assertEquals(1, factory.getCatalog().getSequencesSize());
        assertEquals("alternative_foreignkey_syntax", table.getTableName());
        assertNotNull(table.getPrimaryKey());
        assertEquals(2, table.getForeignKeySize());
        assertNotNull(table.getForeignKey("fk_authorities_users"));
    }
}
