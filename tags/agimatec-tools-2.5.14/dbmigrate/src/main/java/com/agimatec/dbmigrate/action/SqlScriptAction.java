package com.agimatec.dbmigrate.action;

import com.agimatec.dbmigrate.AutoMigrationTool;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.12.2008 <br/>
 * Time: 10:44:36 <br/>
 * Copyright: Agimatec GmbH
 */
public class SqlScriptAction extends ScriptAction {
    public SqlScriptAction(AutoMigrationTool tool, String scriptCmd) {
        super(tool, scriptCmd);
    }

    @Override
    public void doIt() throws Exception {
        if (!tool.isSim()) {
            tool.doSQLScript(getScriptName());
        }
    }

    public String getInfo() {
        return "SQL Script: " + getScriptName();
    }
}
