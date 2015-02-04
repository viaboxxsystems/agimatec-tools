package com.agimatec.commons.config;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

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

    public void testToURLs() throws MalformedURLException {
        URL url = ConfigManager.toURL("cp://subdir/other.xml");
        assertTrue(url.toString().endsWith("subdir/other.xml"));

        url = ConfigManager.toURL("cp://subdir/../unknown.xml");
        assertTrue(url == null);

        url = ConfigManager.toURL("cp://subdir/../migration.xml");
        assertTrue(url != null);
        assertTrue(url.toString().endsWith("migration.xml"));
    }

    public void testToURL_with_dot() throws Exception {
        URL url = ConfigManager.toURL("cp://./testscript.sql");
        assertNotNull(url);
        url = ConfigManager.toURL("cp://subdir/./other.xml");
        assertNotNull(url);
    }
    
    public void testResolvePath() {
        String in = "/root/folder/subdir/../migration.xml";
        String out ="/root/folder/migration.xml";
        assertEquals(out, ConfigManager.resolvePath(in));

        in = "../migration.xml";
        out ="../migration.xml";
        assertEquals(out, ConfigManager.resolvePath(in));


        in = "myfolder/../migration.xml";
        out ="migration.xml";
        assertEquals(out, ConfigManager.resolvePath(in));

        in = "/myfolder/../migration.xml";
        out ="/migration.xml";
        assertEquals(out, ConfigManager.resolvePath(in));

        in = "//myfolder/../migration.xml";
        out ="//migration.xml";
        assertEquals(out, ConfigManager.resolvePath(in));
    }
}
