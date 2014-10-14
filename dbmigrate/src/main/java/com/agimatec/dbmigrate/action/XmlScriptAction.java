package com.agimatec.dbmigrate.action;

import com.agimatec.dbmigrate.AutoMigrationTool;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.12.2008 <br/>
 * Time: 10:44:18 <br/>
 */
public class XmlScriptAction extends ScriptAction {
    public XmlScriptAction(AutoMigrationTool tool, String scriptCmd) {
        super(tool, scriptCmd);
    }

    @Override
    public void doIt() throws Exception {
        String filePath = tool.getScriptsDir() + getScriptName();
        tool.doXmlScript(filePath);
    }

    public String getInfo() {
        String filePath = tool.getScriptsDir() + getScriptName();
        return "XML Config: " + filePath;
    }
}
