package com.agimatec.commons.beans;

import junit.framework.*;

import java.util.Map;
import java.util.HashMap;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
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
    q.parse("platform=BAHN");

    Map map = new HashMap();
    map.put("platform", "BAHN");
    map.put("env", "test");

    assertTrue(q.doesMatch(map));

    map.put("platform", "TQ3");
    assertTrue(!q.doesMatch(map));


    q.parse("platform = TQ3 & env=test ");
    assertTrue(q.doesMatch(map));

  }
}
