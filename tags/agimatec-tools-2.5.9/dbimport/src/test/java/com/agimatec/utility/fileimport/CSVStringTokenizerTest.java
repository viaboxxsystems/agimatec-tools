package com.agimatec.utility.fileimport;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * CSVStringTokenizer Tester.
 *
 * @author Roman Stumm
 * @version 1.0
 * @since <pre>08/28/2007</pre>
 */
public class CSVStringTokenizerTest extends TestCase {

    public CSVStringTokenizerTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(CSVStringTokenizerTest.class);
    }

    public void testCSVStringTokenizer1() {
        String s;
        CSVStringTokenizer t;

        s = "a;be;ce";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("a", t.nextElement());
        assertEquals("be", t.nextElement());
        assertEquals("ce", t.nextElement());
        assertEquals(false, t.hasMoreElements());

        s = "a;be;ce;";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("a", t.nextElement());
        assertEquals("be", t.nextElement());
        assertEquals("ce", t.nextElement());
        assertEquals(true, t.hasMoreElements());
        assertEquals("", t.nextElement());
        assertEquals(false, t.hasMoreElements());

        s = "a;be;ce;;";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("a", t.nextElement());
        assertEquals("be", t.nextElement());
        assertEquals("ce", t.nextElement());
        assertEquals(true, t.hasMoreElements());
        assertEquals("", t.nextElement());
        assertEquals(true, t.hasMoreElements());
        assertEquals("", t.nextElement());
        assertEquals(false, t.hasMoreElements());
    }

    public void testCSVStringTokenizer2() {
        String s;
        CSVStringTokenizer t;

        s = ";be;ce";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("", t.nextElement());
        assertEquals("be", t.nextElement());
        assertEquals("ce", t.nextElement());
        assertEquals(false, t.hasMoreElements());
    }

    public void testCSVStringTokenizer3() {
        String s;
        CSVStringTokenizer t;

        s = ";be;d;;ce";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("", t.nextElement());
        assertEquals("be", t.nextElement());
        assertEquals("d", t.nextElement());
        assertEquals("", t.nextElement());
        assertEquals("ce", t.nextElement());
        assertEquals(false, t.hasMoreElements());
    }

    public void testCSVStringTokenizer4() {
        String s;
        CSVStringTokenizer t;

        s = ";be;\"d;2\";;ce";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("", t.nextElement());
        assertEquals("be", t.nextElement());
        assertEquals("d;2", t.nextElement());
        assertEquals("", t.nextElement());
        assertEquals("ce", t.nextElement());
        assertEquals(false, t.hasMoreElements());
    }

    public void testCSVStringTokenizer5() {
        String s;
        CSVStringTokenizer t;

        s = "be;\"d;2\";;\"ce;x\";\"ff\"\"ss\"\"\";\";;;\"\";\";ende";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("be", t.nextElement());
        assertEquals("d;2", t.nextElement());
        assertEquals("", t.nextElement());
        assertEquals(true, t.hasMoreElements());
        assertEquals("ce;x", t.nextElement());
        assertEquals(true, t.hasMoreElements());
        assertEquals("ff\"ss\"", t.nextElement());
        assertEquals(";;;\";", t.nextElement());
        assertEquals("ende", t.nextElement());
        assertEquals(false, t.hasMoreElements());
    }

    public void testCSVStringTokenizer6() {
        String s;
        CSVStringTokenizer t;

        s = "a;\"c;\";\"d;e\"";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("a", t.nextElement());
        assertEquals("c;", t.nextElement());
        assertEquals("d;e", t.nextElement());
        assertEquals(false, t.hasMoreElements());

        s = "a;\"c;\";\"d;e\";";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("a", t.nextElement());
        assertEquals("c;", t.nextElement());
        assertEquals("d;e", t.nextElement());
        assertEquals("", t.nextElement());
        assertEquals(false, t.hasMoreElements());

        s = "a;\"c;\";\"d;e\";;";
        t = new CSVStringTokenizer(s, ";");
        assertTrue(t.hasMoreElements());
        assertEquals("a", t.nextElement());
        assertEquals("c;", t.nextElement());
        assertEquals("d;e", t.nextElement());
        assertEquals("", t.nextElement());
        assertEquals("", t.nextElement());
        assertEquals(false, t.hasMoreElements());
    }

    public void testCSVMultiLineRecord() {
        String csvline =
                "\"2FPAK_________GJ_\";\"1\";\"1\";\"4\";\"2\";\"Zentrum\";\"Hauptstasse\";\"Bahnhofsparkplatz\";;\"78734\";\"Augsburg\";;\"DE\";\"der safe befindet sich auf dem\n" +
                        "                        Schliessfach 12\";;;;;;\"1\";\n";
        CSVStringTokenizer tokens = new CSVStringTokenizer(csvline, ";");
        int i = 0;
        List strings = new ArrayList();
        while (tokens.hasMoreElements()) {
            String each = tokens.nextElement();
            strings.add(each);
            i++;
        }
        assertEquals(21, i);
        assertEquals("der safe befindet sich auf dem\n" +
                "                        Schliessfach 12", strings.get(13));
    }

    public void testCSVMultiLine_DetectHalfLine() {
        String halfline =
                "\"2FPAK_________GJ_\";\"1\";\"1\";\"4\";\"2\";\"Zentrum\";\"Hauptstasse\";\"Bahnhofsparkplatz\";;\"78734\";\"Augsburg\";;\"DE\";\"der safe befindet sich auf dem";
        CSVStringTokenizer tokens = new CSVStringTokenizer(halfline, ";");
        int i = 0;
        List<String> strings = new ArrayList();
        while (tokens.hasMoreElements()) {
            String each = tokens.nextElement();
            strings.add(each);
            i++;
        }
        assertEquals(14, i);
        assertEquals("der safe befindet sich auf dem", strings.get(13));
        assertEquals(true, tokens.isLineIncomplete());
        String secondhalf = "                        Schliessfach 12\n\nja\";nextval;";
        String theWhole = (String) tokens.continueParse(strings.get(13), secondhalf);
        assertEquals(
                "der safe befindet sich auf dem\n                        Schliessfach 12\n\nja",
                theWhole);
        assertEquals("nextval", tokens.nextElement());
        assertEquals("", tokens.nextElement());
        assertEquals(false, tokens.hasMoreElements());

        halfline = "flug:\nherr meier\n\nmachts";
        tokens = new CSVStringTokenizer(halfline, ";");
        String token = tokens.nextElement();
        assertEquals(halfline, token);
        assertFalse(tokens.isLineIncomplete());
    }
}

