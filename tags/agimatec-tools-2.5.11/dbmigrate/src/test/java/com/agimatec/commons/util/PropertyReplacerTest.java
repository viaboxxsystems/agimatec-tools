package com.agimatec.commons.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Properties;

/**
 * PropertyReplacer Tester.
 *
 * @author ${USER}
 * @since <pre>05/03/2007</pre>
 * @version 1.0
 */
public class PropertyReplacerTest extends TestCase {
    public PropertyReplacerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetProperties() throws Exception {
        Properties props = new Properties();
        props.setProperty("user", "Roman");
        props.setProperty("password", "${xx}");
        PropertyReplacer repl = new PropertyReplacer(props);
        String result = repl.replaceProperties("Hello ${user}. The $BODY$ stays unchanged $$. Password=${password}");
        assertEquals("Hello Roman. The $BODY$ stays unchanged $$. Password=${xx}", result);
    }

    public static Test suite() {
        return new TestSuite(PropertyReplacerTest.class);
    }
}
