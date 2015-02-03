package com.agimatec.dbmigrate.action;

import com.agimatec.dbmigrate.AutoMigrationTool;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.12.2008 <br/>
 * Time: 10:44:11 <br/>
 */
public class GroovyScriptAction extends ScriptAction {
    public GroovyScriptAction(AutoMigrationTool tool, String scriptCmd) {
        super(tool, scriptCmd);
    }

    @Override
    public void doIt() throws Exception {
        if (!tool.isSim()) {
            tool.doGroovyScript(getScriptName());
        }
    }

    public String getInfo() {
        return "Groovy Script: " + getScriptName();
    }
}
