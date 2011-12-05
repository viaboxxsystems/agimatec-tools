package com.agimatec.dbtransform.ejb3;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * Ejb3Schema Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>07/03/2007</pre>
 */
public class Ejb3SchemaTest extends TestCase {
    private CatalogDescription catalog;

    public Ejb3SchemaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        catalog = loadCatalog();
    }

    private CatalogDescription loadCatalog() throws IOException, ClassNotFoundException {
        return (CatalogDescription) new XStreamPersistencer()
                .load(new File("src/test/resources/catalog-example.xml"));
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetCatalog() throws Exception {
        assertNotNull(catalog);
        Ejb3Schema schema = new Ejb3Schema(catalog);
        schema.generate();
        assertNotNull(schema.getEjb3classes());
    }

    public static Test suite() {
        return new TestSuite(Ejb3SchemaTest.class);
    }
}
