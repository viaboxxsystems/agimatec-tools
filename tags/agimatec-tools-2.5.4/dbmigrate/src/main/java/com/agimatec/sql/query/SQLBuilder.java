package com.agimatec.sql.query;

import com.agimatec.sql.SQLStatement;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Description: Radikal abgespeckte Funktionalität:
 * Diese klasse ist nur noch ein Schatten ihrer selbst!
 * Hier werden nur noch einfache Statements aus einer .properties Datei geholt, mehr
 * wenn nötig später...<br/>
 * User: roman.stumm <br/>
 * Date: 09.11.2007 <br/>
 * Time: 15:08:00 <br/>
 * Copyright: Agimatec GmbH
 */
public class SQLBuilder {
    private final Properties querySpecs;

    public SQLBuilder(String resourceName) throws IOException {
        this.querySpecs = new Properties();
        querySpecs.load(getClass().getClassLoader().getResourceAsStream(resourceName));
    }

    public String getResultBuilderName(String queryName) {
        return querySpecs.getProperty(queryName + ".resultbuilder");
    }

    public String getSQL(String queryName) {
        return querySpecs.getProperty(queryName + ".sql");
    }

    public SQLStatement generateSQL(QueryDefinition queryDefinition) {
        String sql = getSQL(queryDefinition.getQueryName());
        SQLStatement stmt = new SQLStatement(sql);
        int params = StringUtils.countMatches(sql, "?");
        if (queryDefinition.getQueryObject() instanceof List) {
            List paramValues = (List) queryDefinition.getQueryObject();
            for (int i = 0; i < params; i++) {
                stmt.addParameter(paramValues.get(i));
            }
        } else {
            for (int i = 0; i < params; i++) {
                stmt.addParameter(queryDefinition.getQueryObject());
            }
        }
        return stmt;
    }
}
