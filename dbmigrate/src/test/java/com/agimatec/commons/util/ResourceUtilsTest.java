package com.agimatec.commons.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.03.2010<br>
 * Time: 10:37:13<br>
 * viaboxx GmbH, 2010
 */
public class ResourceUtilsTest extends TestCase {

  public void testURLUtils() throws IOException {
    List<String> list = ResourceUtils.readLines("com/");
    for (String name : list) {
      System.out.println(name);
    }
  }

}
