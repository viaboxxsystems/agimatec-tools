package com.agimatec.utility.fileimport;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Importer Tester.
 *
 * @author ${USER}
 * @since <pre>08/28/2007</pre>
 * @version 1.0
 */
public class ImporterTest extends TestCase {
    public ImporterTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testImportFromCsv() throws Exception {
        LineImporterSpec spec = new LineImporterSpecAutoFields();
        spec.setLineTokenizerFactory(new CSVStringTokenizerFactory());
        Importer importer = new Importer(spec);
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("example-x0304p.txt");
        Reader reader = new InputStreamReader(stream, "ISO-8859-1");
        importer.importFrom(reader);
    }

    public void testImportFromXml() throws Exception {
        GroovyScriptEngine engine = new GroovyScriptEngine("src/test/resources");
        Binding binding = new Binding();
        Object result = engine.run("ImportFromXmlTest.groovy", binding);
        assertEquals(new Integer(3), result);
    }

    public static Test suite() {
        return new TestSuite(ImporterTest.class);
    }
}
