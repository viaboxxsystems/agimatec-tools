package com.agimatec.dbmigrate;

import com.agimatec.commons.config.ConfigManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.util.List;

/**
 * BaseMigrationTool Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>05/04/2007</pre>
 */
public class BaseMigrationToolTest extends TestCase {
    private BaseMigrationTool tool;

    public BaseMigrationToolTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        tool = new AutoMigrationTool();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFindMethod() {
        Method m = tool.findMethod(tool.getClass(), "invokeBean", 1);
        assertNotNull(m);
        m = tool.findMethod(tool.getClass(), "invokeBean", 2);
        assertNull(m);
    }

    public void testSplitMethodArgs() {
        Object[] result = tool.splitMethodArgs(
                tool.getClass().getName() + "#" + "splitMethodArgs(test1,test2)");
        assertEquals(3, result.length);
        assertEquals(tool.getClass().getName(), result[0]);
        assertEquals("splitMethodArgs", result[1]);
        assertEquals("test1", ((List)result[2]).get(0));
        assertEquals("test2", ((List)result[2]).get(1));
    }

    public void testReplacePropertiesInEnvironment() {
        ConfigManager.getDefault().setConfigRootPath("cp://");
        assertEquals("jdbc:postgresql://localhost:5432/test", tool.getEnvironment().get("DB_URL"));
    }

    public static Test suite() {
        return new TestSuite(BaseMigrationToolTest.class);
    }
}
