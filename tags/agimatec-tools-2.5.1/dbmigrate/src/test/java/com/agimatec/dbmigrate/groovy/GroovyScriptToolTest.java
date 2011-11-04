package com.agimatec.dbmigrate.groovy;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 23.09.2010<br>
 * Time: 17:48:08<br>
 * viaboxx GmbH, 2010
 */
public class GroovyScriptToolTest extends TestCase {

    public void testLookup() throws IOException, ScriptException, ResourceException {
        GroovyScriptTool tool = new GroovyScriptTool("cp://");
        tool.start("scripts1/Script1.groovy");
        tool.start("scripts2/Script2.groovy");
        tool.start("scripts1/scripts3/Script3.groovy");

        tool = new GroovyScriptTool("cp://scripts1/");
        tool.start("Script1.groovy");
        tool.start("scripts3/Script3.groovy");


        tool = new GroovyScriptTool(new String[] {"cp://scripts1/", "cp://scripts2/", "cp://scripts1/scripts3/"});
        tool.start("Script1.groovy");
        tool.start("Script2.groovy");
        tool.start("Script3.groovy");
    }
}
