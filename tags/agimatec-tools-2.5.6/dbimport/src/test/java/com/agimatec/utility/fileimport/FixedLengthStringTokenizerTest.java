package com.agimatec.utility.fileimport;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * FixedLengthStringTokenizer Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>09/11/2007</pre>
 */
public class FixedLengthStringTokenizerTest extends TestCase {

    public FixedLengthStringTokenizerTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(FixedLengthStringTokenizerTest.class);
    }

    public void testParse() {
        FixedLengthStringTokenizer myTokenizer =
                new FixedLengthStringTokenizer(getTestString(), getTestConfig());
        String[] tokens = new String[]{"1234", "Hallo Welt", "", "20030310", "Bahnhof"};
        for (String token : tokens) {
            Object each = myTokenizer.nextElement();
            assertEquals(token, each);
        }
        assertEquals(false, myTokenizer.hasMoreElements());
    }

    private int[] getTestConfig() {
        // negative Werte stehen für absolute Anzahl Zeichen, die zu ignoreren sind
        return new int[]{-1, 4, -1, 10, 6, -1, 8, 10, -1};
    }

    private String getTestString() {
        return "|1234|Hallo Welt      |20030310Bahnhof   |";
    }

}
