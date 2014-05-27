package com.agimatec.commons.generator;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.File;

import com.agimatec.commons.generator.FreemarkerFileGenerator;

/**
 * FreemarkerFileGenerator Tester.
 *
 * @author ${USER}
 * @since <pre>07/04/2007</pre>
 * @version 1.0
 */
public class FreemarkerFileGeneratorTest extends TestCase {
    public FreemarkerFileGeneratorTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetFreemarker() throws Exception {
        FreemarkerFileGenerator gen = new FreemarkerFileGenerator(new File("src/test/resources"));
        gen.setBaseDir("target");
        gen.setDestFileName(null);
        gen.setTemplateName("test-template.ftl");
        gen.generate();
        assertTrue(new File("target/file1.txt").exists());
        assertTrue(new File("target/file2.txt").exists());
        assertEquals("file2.txt", gen.getDestFile().getName());
    }

    public static Test suite() {
        return new TestSuite(FreemarkerFileGeneratorTest.class);
    }
}
