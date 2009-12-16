package com.agimatec.dbmigrate;

import com.agimatec.jdbc.JdbcDatabase;

import java.util.Map;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 13.11.2007 <br/>
 * Time: 16:18:39 <br/>
 * Copyright: Agimatec GmbH
 */
public interface MigrationTool {
    JdbcDatabase getTargetDatabase();

    Map getEnvironment();
}
