package com.agimatec.dbhistory;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * HistoryGenerator Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>04/27/2007</pre>
 */
public class HistoryGeneratorToolTest extends TestCase {
    public HistoryGeneratorToolTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public static Test suite() {
        return new TestSuite(HistoryGeneratorToolTest.class);
    }

    public void testSaveHistoryConfig() throws IOException {
        HistSchemaConfig conf = new HistSchemaConfig();
        HistTableConfig tc = new HistTableConfig("user_core");
        tc.getExcludeColumns().add("registrationTime");
        conf.addTableConfig(tc);        
        conf.addTableConfig(new HistTableConfig("user_core"));
        conf.addTableConfig(new HistTableConfig("Privilege"));
        conf.addTableConfig(new HistTableConfig("Address_1"));
        new XStreamPersistencer()
                .save(conf, new File("target/historyconfig-example.xml"));
    }

    public void testHistoryGenerator() throws Exception {

        File hconf = new File("src/test/resources/historyconfig-example.xml");
        HistoryGeneratorTool generator = new HistoryGeneratorTool();
        generator.readConfig(hconf);
        CatalogDescription catalog = (CatalogDescription) new XStreamPersistencer()
                .load(new File("src/test/resources/catalog-example.xml"));
        HistoryGeneratorSettings settings = new HistoryGeneratorSettings();
        settings.setTemplateDir("templates");
        settings.setDbms("postgres");
        settings.setCatalog(catalog);
        settings.setDestDir("target");
        generator.initialize(settings);
        generator.generate("history-schema");
        generator.generate("history-triggers");
    }
}
