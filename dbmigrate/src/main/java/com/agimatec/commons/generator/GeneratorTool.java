package com.agimatec.commons.generator;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Description: Abstract superclass for tools that invoke freemarker templates for
 * a catalogdescription<br/>
 * User: roman.stumm <br/>
 * Date: 05.06.2007 <br/>
 * Time: 15:39:09 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class GeneratorTool {
    protected GeneratorSettings settings;
    protected FreemarkerFileGenerator templateEngine;
    protected String dbms;

    public void generate(String templateBaseName) throws IOException, TemplateException {
        templateEngine.setBaseDir(settings.getDestDir());
        if (settings.isNoOutputFile()) {
            templateEngine.setDestFileName(null);
        } else {
            templateEngine.setDestFileName(getDestFile(templateBaseName));
        }
        templateEngine.setTemplateName(templateBaseName + ".ftl");
        templateEngine.generate();
    }

    public void initialize(GeneratorSettings settings) throws IOException {
        this.settings = settings;
        dbms = settings.getDbms();
        templateEngine = new FreemarkerFileGenerator(
              new File(settings.getTemplateDir() + "/" + dbms));
        templateEngine.putModel("catalog", settings.getCatalog());
        if (getConfig() != null) templateEngine.putModel("config", getConfig());
        templateEngine.putModel("dbms", dbms);
        if (settings.getProperties() != null && !settings.getProperties().isEmpty()) {
            for (Map.Entry<String, String> entry : settings.getProperties()
                  .entrySet()) {
                templateEngine.putModel(entry.getKey(), entry.getValue());
            }
        }
    }

    protected abstract Object getConfig();

    protected abstract void readConfig(File file)
          throws IOException, ClassNotFoundException;

    protected String getDestFile(String templateBaseName) {
        return templateBaseName + "-" + dbms + ".sql";
    }

    public void runMain(String[] args, GeneratorSettings settings) throws Exception {
        if (!settings.parseArgs(args)) return;
        readConfig(settings.getConfigFile() == null ? null :
              new File(settings.getConfigFile()));
        initialize(settings);
        for (String template : settings.getTemplates()) {
            generate(template);
        }
    }


}
