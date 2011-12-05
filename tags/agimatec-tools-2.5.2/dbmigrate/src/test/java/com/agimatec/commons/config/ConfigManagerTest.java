package com.agimatec.commons.config;

import junit.framework.TestCase;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.03.2010<br>
 * Time: 12:06:02<br>
 * viaboxx GmbH, 2010
 */
public class ConfigManagerTest extends TestCase {
  public void testSetConfigRoot() {
    ConfigManager cmg = new ConfigManager(null);
    cmg.setConfigRootPath("cp://subdir/");

    Config found = cmg.getConfig("migration", "somefile.xml");
    assertNotNull(found);
    String fp = found.getFilePath("other");
    assertNotNull(fp);
    assertEquals("cp://subdir/other.xml", fp);
  }
}
