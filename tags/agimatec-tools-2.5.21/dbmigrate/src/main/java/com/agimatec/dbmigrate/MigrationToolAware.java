package com.agimatec.dbmigrate;

/**
 * Description: Callback interface<br/>
 * User: roman.stumm <br/>
 * Date: 14.05.2007 <br/>
 * Time: 10:59:31 <br/>
 */
public interface MigrationToolAware {
    void setMigrationTool(MigrationTool tool);
}
