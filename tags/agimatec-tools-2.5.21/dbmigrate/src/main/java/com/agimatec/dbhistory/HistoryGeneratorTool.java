package com.agimatec.dbhistory;

import com.agimatec.commons.generator.GeneratorTool;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;

import java.io.File;
import java.io.IOException;

/**
 * Description: Tool to generate files with freemarker for a catalogdescription
 * and a history configuration.<br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 18:08:23 <br/>
 */
public class HistoryGeneratorTool extends GeneratorTool {
    private HistSchemaConfig config;

    protected HistSchemaConfig getConfig() {
        return config;
    }

    protected void readConfig(File historyConfigFile) throws IOException,
            ClassNotFoundException {
        config = (HistSchemaConfig) new XStreamPersistencer().load(historyConfigFile);
    }
    
    public static void main(String[] args) throws Exception {
        HistoryGeneratorTool tool = new HistoryGeneratorTool();
        tool.runMain(args, new HistoryGeneratorSettings());
    }

}
