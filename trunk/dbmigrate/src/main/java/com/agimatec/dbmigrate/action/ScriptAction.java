package com.agimatec.dbmigrate.action;

import com.agimatec.dbmigrate.AutoMigrationTool;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.12.2008 <br/>
 * Time: 10:26:14 <br/>
 */
public abstract class ScriptAction extends MigrateAction {
    protected String scriptName;

    public static enum FileFormat {
        XML,
        SQL,
        JDBC,
        STMT,
        GROOVY
    }

    public static ScriptAction create(AutoMigrationTool tool, String fileName, String fileType) {
        if (FileFormat.XML.name().equalsIgnoreCase(fileType)) {
            return new XmlScriptAction(tool, fileName);
        } else if (FileFormat.SQL.name().equalsIgnoreCase(fileType)) {
            return new SqlScriptAction(tool, fileName);
        } else if (FileFormat.JDBC.name().equalsIgnoreCase(fileType)) {
            return new SqlLinesAction(tool, fileName);
        } else if (FileFormat.STMT.name().equalsIgnoreCase(fileType)) {
            return new SqlExecAction(tool, fileName);
        } else if (FileFormat.GROOVY.name().equalsIgnoreCase(fileType)) {
            return new GroovyScriptAction(tool, fileName);
        } else {
            tool.log("not a supported file type: " + fileName);
            return null;
        }
    }

    public static ScriptAction create(AutoMigrationTool tool, String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) return null;
        String fileType = fileName.substring(idx + 1);
        return create(tool, fileName, fileType);
    }

    protected ScriptAction(AutoMigrationTool tool, String scriptName) {
        super(tool);
        this.scriptName = scriptName;
    }

    public String getScriptName() {
        return scriptName;
    }
}
