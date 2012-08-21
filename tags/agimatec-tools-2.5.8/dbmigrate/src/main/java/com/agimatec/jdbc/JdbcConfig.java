package com.agimatec.jdbc;

import com.agimatec.commons.config.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 12:34:49 <br/>
 * Copyright: Agimatec GmbH
 */
public class JdbcConfig {
    private final Properties properties = new Properties();
    static final String JDBC_URL = "jdbcUrl";
    static final String JDBC_DRIVER = "jdbcDriver";

    /**
     * @param aURL - filename with .properties to load
     * @throws IOException
     */
    public void read(String aURL) {
        try {
            InputStream in = ConfigManager.toURL(aURL).openStream();
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new JdbcException(e);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getConnect() {
        return properties.getProperty(JDBC_URL);
    }

    public void setConnect(String urlConnect) {
        properties.setProperty(JDBC_URL, urlConnect);
    }

    public String getDriver() {
      return properties.getProperty(JDBC_DRIVER);
    }
  
    public void setDriver(String dr) {
      properties.setProperty(JDBC_DRIVER, dr);
    }
}
