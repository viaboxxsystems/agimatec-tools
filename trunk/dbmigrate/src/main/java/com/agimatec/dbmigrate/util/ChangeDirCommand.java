package com.agimatec.dbmigrate.util;

/**
 * Description: HACK to switch the scriptDir for before and after scripts<br/>
 * User: roman.stumm <br/>
 * Date: 09.11.2007 <br/>
 * Time: 11:54:53 <br/>
 * Copyright: Agimatec GmbH
 */
public class ChangeDirCommand {
    String dir;

    public ChangeDirCommand(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
