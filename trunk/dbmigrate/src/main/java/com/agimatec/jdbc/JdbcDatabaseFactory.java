package com.agimatec.jdbc;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 12:36:28 <br/>
 */
public class JdbcDatabaseFactory {
    public static JdbcDatabase createInstance(JdbcConfig storeCfg) {
        return new JdbcDatabaseImpl(storeCfg.getProperties());
    }
}
