package com.agimatec.dbmigrate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBVersionStringTest extends TestCase {

    public DBVersionStringTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(DBVersionStringTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOther() throws Exception {
        DBVersionString v = new DBVersionString("0001.sql");
        assertEquals(1, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(0, v.getIncrement());
        assertEquals(".sql", v.getRest());
        assertEquals("sql", v.getFileType());

        v = new DBVersionString("1_test.sql");
        assertEquals(1, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(0, v.getIncrement());
        assertEquals("_test.sql", v.getRest());
        assertEquals("sql", v.getFileType());

        v = new DBVersionString("1_test_more.sql");
        assertEquals(1, v.getMajor());
        assertEquals(0, v.getMinor());
        assertEquals(0, v.getIncrement());
        assertEquals("_test_more.sql", v.getRest());
        assertEquals("sql", v.getFileType());

        assertEquals(null, DBVersionString.fromString("notAVersion.sql"));
        assertEquals(null, DBVersionString.fromString(""));
    }

    public void testFromString() throws Exception {
        DBVersionString v = new DBVersionString("6.1.45_test.sql");
        assertEquals(6, v.getMajor());
        assertEquals(1, v.getMinor());
        assertEquals(45, v.getIncrement());
        assertEquals("_test.sql", v.getRest());
        assertEquals("sql", v.getFileType());
        //
        v = new DBVersionString("06.001.045.sql");
        assertEquals(6, v.getMajor());
        assertEquals(1, v.getMinor());
        assertEquals(45, v.getIncrement());
        assertEquals(".sql", v.getRest());
        assertEquals("sql", v.getFileType());

        //
        v = new DBVersionString("06.001.045");
        assertEquals(6, v.getMajor());
        assertEquals(1, v.getMinor());
        assertEquals(45, v.getIncrement());
        assertEquals("", v.getRest());
        assertEquals(null, v.getFileType());

        //
        v = new DBVersionString("6.1");
        assertEquals(6, v.getMajor());
        assertEquals(1, v.getMinor());
        assertEquals(0, v.getIncrement());
        assertEquals("", v.getRest());
        assertEquals(null, v.getFileType());

        //
        v = new DBVersionString("6.1_test.sql");
        assertEquals(6, v.getMajor());
        assertEquals(1, v.getMinor());
        assertEquals(0, v.getIncrement());
        assertEquals("_test.sql", v.getRest());
        assertEquals("sql", v.getFileType());

        //
        v = new DBVersionString("6.1.xml");
        assertEquals(6, v.getMajor());
        assertEquals(1, v.getMinor());
        assertEquals(0, v.getIncrement());
        assertEquals(".xml", v.getRest());
        assertEquals("xml", v.getFileType());

        //
        v = new DBVersionString("6.2.2.xml");
        assertEquals(6, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(2, v.getIncrement());
        assertEquals(".xml", v.getRest());
        assertEquals("xml", v.getFileType());
    }

    public void testDBVersionComparator() {
        assertTrue(new DBVersionString("6.0.0.sql")
                .compareTo(new DBVersionString("6.0.1.sql")) < 0);

        List<DBVersionString> versions = new ArrayList<DBVersionString>();
        versions.add(new DBVersionString("6.1.2_a.sql"));
        versions.add(new DBVersionString("6.0.1_a.sql"));
        versions.add(new DBVersionString("6.0.0_b.sql"));
        versions.add(new DBVersionString("6.0.0_a.sql"));
        versions.add(new DBVersionString("6.1.1_a.sql"));
        versions.add(new DBVersionString("6.1.1_xxx.xml"));
        versions.add(new DBVersionString("6.1.1_aaa.xml"));
        versions.add(new DBVersionString("6.0.2_a.sql"));
        versions.add(new DBVersionString("6.0.2_x.sql"));
        versions.add(new DBVersionString("6.0.2_c.sql"));
        versions.add(new DBVersionString("6.0.2_e.sql"));
        versions.add(new DBVersionString("6.0.2_b.sql"));
        Collections.sort(versions);
        for (DBVersionString each : versions) {
            System.out.println(each);
        }

    }
}