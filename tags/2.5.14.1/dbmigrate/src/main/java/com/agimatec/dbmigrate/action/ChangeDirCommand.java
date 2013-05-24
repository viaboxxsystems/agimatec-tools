package com.agimatec.dbmigrate.action;

import com.agimatec.dbmigrate.AutoMigrationTool;

/**
 * Description: HACK to switch the scriptDir for before and after scripts<br/>
 * User: roman.stumm <br/>
 * Date: 09.11.2007 <br/>
 * Time: 11:54:53 <br/>
 * Copyright: Agimatec GmbH
 */
public class ChangeDirCommand extends MigrateAction {
    String dir;

    public ChangeDirCommand(AutoMigrationTool tool, String dir) {
        super(tool);
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void doIt() {
        tool.log("changing script directory to " + getDir());
        tool.setScriptsDir(getDir());
    }

    @Override
    public String getInfo() {
        return "cd " + getDir();                 
    }
}
