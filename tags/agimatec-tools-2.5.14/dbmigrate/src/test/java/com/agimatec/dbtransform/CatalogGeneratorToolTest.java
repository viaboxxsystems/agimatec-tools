package com.agimatec.dbtransform;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

/**
 * CatalogGeneratorTool Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>06/05/2007</pre>
 */
public class CatalogGeneratorToolTest extends TestCase {
    public CatalogGeneratorToolTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(CatalogGeneratorToolTest.class);
    }

    public void testSaveConfigGenerate() throws Exception {
        CatalogConversion conf = new CatalogConversion("Postgres to Oracle");
        conf.setMaxLengthForConstraints(30);
        conf.setFilterIndices(true);
        conf.addTransformation("CHARACTER VARYING", "VARCHAR2");
        conf.addTransformation("TEXT", "CLOB");
        conf.addTransformation("BYTEA", "BLOB");
        conf.addTransformation("BIGINT", "INTEGER");
        conf.addTransformation(new DataType("BOOLEAN"), new DataType("NUMBER", 1));
        DataType dt = new DataType("CLOB");
        dt.setPrecisionEnabled(Boolean.FALSE);
        conf.addTransformation(new DataType("VARCHAR", 10000), dt);
        conf.addTransformation("VARCHAR", "VARCHAR2");
        new XStreamPersistencer()
                .save(conf, new File("target/db-conversion.xml"));

        File hconf = new File("target/db-conversion.xml");
        CatalogGeneratorTool generator = new CatalogGeneratorTool();
        generator.readConfig(hconf);
        CatalogDescription catalog = (CatalogDescription) new XStreamPersistencer()
                .load(new File("src/test/resources/catalog-example.xml"));
//                .load(new File("src/test/resources/nucleus-catalog-postgres.xml"));
        CatalogGeneratorSettings settings = new CatalogGeneratorSettings();
        settings.setTemplateDir("templates");
        settings.setDbms("oracle");
        settings.setCatalog(catalog);
        settings.getCatalog().setSchemaName("nucleus");
        settings.setDestDir("target");
//        settings.setCatalogFile("target/nucleus-catalog-oracle.xml");
        settings.setCatalogFile("target/catalog-example-oracle.xml");
        generator.initialize(settings);
        generator.generate("create-tables");
    }
}
