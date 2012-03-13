package com.agimatec.sql.meta.script;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.StringTokenizer;

/**
 * RevertableStringTokenizer Tester.
 *
 * @author ${USER}
 * @since <pre>12/17/2007</pre>
 * @version 1.0
 */
public class RevertableStringTokenizerTest extends TestCase {
    public RevertableStringTokenizerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetIndex() throws Exception {
        RevertableStringTokenizer tokens =
                new RevertableStringTokenizer(new StringTokenizer("this is a test", " "));
        String[] expect = { "this", "is", "a", "test" };
        int i=0;
        while(tokens.hasMoreTokens()) {
            String each = tokens.nextToken();
            assertEquals(expect[i++], each);
        }
        assertEquals(4, tokens.getPosition());
        assertEquals(false, tokens.hasMoreTokens());
        tokens.setPosition(2);
        assertEquals(true, tokens.hasMoreTokens());
        assertEquals("a", tokens.nextElement());
        assertEquals(true, tokens.hasMoreTokens());
        assertEquals("test", tokens.nextElement());
        assertEquals(false, tokens.hasMoreTokens());
    }

    public static Test suite() {
        return new TestSuite(RevertableStringTokenizerTest.class);
    }
}
