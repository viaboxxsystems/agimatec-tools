package com.agimatec.sql.meta.script;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.sql.meta.persistence.ObjectPersistencer;
import com.agimatec.sql.meta.persistence.SerializerPersistencer;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.net.URL;

/**
 * DDLScriptSqlMetaFactory Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>04/24/2007</pre>
 */
public class DDLScriptSqlMetaFactoryTest extends TestCase {
    public DDLScriptSqlMetaFactoryTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFillCatalog()
            throws Exception {
        DDLScriptSqlMetaFactory factory = new DDLScriptSqlMetaFactory(
                DDLExpressions.forDbms("postgres"));
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

    public static Test suite() {
        return new TestSuite(DDLScriptSqlMetaFactoryTest.class);
    }
}
