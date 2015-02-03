package com.agimatec.commons.beans;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 *
 * @author Roman Stumm
 */
public class MapQueryTest extends TestCase {

    public MapQueryTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MapQueryTest.class);
    }

    public void testParseQuery() throws Exception {
        MapQuery q = new MapQuery();
        q.parse("platform=XYZ");

        Map map = new HashMap();
        map.put("platform", "XYZ");
        map.put("env", "test");

        assertTrue(q.doesMatch(map));

        map.put("platform", "ABC");
        assertTrue(!q.doesMatch(map));


        q.parse("platform = ABC & env=test ");
        assertTrue(q.doesMatch(map));

    }

    public void testAndOr() throws Exception {
        MapQuery q = new MapQuery();
        q.parse(
              "development_enabled=true & DBMS=postgres | location_management_only=true");
        Map map = new HashMap();
        map.put("development_enabled", "false");
        map.put("DBMS", "postgres");
        map.put("location_management_only", "true");
        assertTrue(q.doesMatch(map));
    }
}
