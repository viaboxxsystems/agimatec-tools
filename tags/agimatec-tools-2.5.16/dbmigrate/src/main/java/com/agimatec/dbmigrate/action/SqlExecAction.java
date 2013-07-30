package com.agimatec.dbmigrate.action;

import com.agimatec.dbmigrate.AutoMigrationTool;

/**
 * Description: <br>
 * <p>
 * Date: 19.06.13<br>
 * </p>
 */
public class SqlExecAction extends ScriptAction {
    public SqlExecAction(AutoMigrationTool tool, String scriptCmd) {
        super(tool, scriptCmd);
    }

    @Override
    public void doIt() throws Exception {
        if (!tool.isSim()) {
            tool.execSQLScript(getScriptName());
        }
    }

    public String getInfo() {
        return "Exec SQL: " + getScriptName();
    }
}