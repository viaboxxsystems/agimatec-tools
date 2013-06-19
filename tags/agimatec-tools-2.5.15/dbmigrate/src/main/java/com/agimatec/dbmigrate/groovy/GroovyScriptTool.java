package com.agimatec.dbmigrate.groovy;

import com.agimatec.commons.config.ConfigManager;
import com.agimatec.commons.util.ClassUtils;
import com.agimatec.dbmigrate.MigrationTool;
import com.agimatec.dbmigrate.MigrationToolAware;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: invoke a groovy script with the migration tool<br/>
 * User: roman.stumm <br/>
 * Date: 13.11.2007 <br/>
 * Time: 16:09:13 <br/>
 * Copyright: Agimatec GmbH
 */
public class GroovyScriptTool implements MigrationToolAware {
    private GroovyScriptEngine scriptEngine;
    private Binding binding = new Binding();

    public GroovyScriptTool(String rootDir) throws IOException {
        if (rootDir == null) {
            scriptEngineFromCP();
        } else {
            scriptEngineFromDirs(new String[]{rootDir});
        }
    }

    public GroovyScriptTool(String[] rootDirs) throws IOException {
        if (rootDirs == null) {
            scriptEngineFromCP();
        } else {
            scriptEngineFromDirs(rootDirs);
        }
    }

    private void scriptEngineFromDirs(String[] rootDirs) throws IOException {
        List<URL> urls = new ArrayList<URL>(rootDirs.length * 3);
        for (String each : rootDirs) {
            urls.addAll(ConfigManager.toURLs(each));
        }
        scriptEngine = new GroovyScriptEngine(
                urls.toArray(new URL[urls.size()]), ClassUtils.getClassLoader());
    }

    private void scriptEngineFromCP() throws IOException {
        List<URL> urls = ConfigManager.toURLs("cp://");
        scriptEngine = new GroovyScriptEngine(
                urls.toArray(new URL[urls.size()]), ClassUtils.getClassLoader());
    }

    public void start(String groovyScript) throws ScriptException, ResourceException {
        scriptEngine.run(groovyScript, binding);
    }

    public void setMigrationTool(MigrationTool tool) {
        binding.setVariable("tool", tool);
    }

    public Binding getBinding() {
        return binding;
    }

    public GroovyScriptEngine getScriptEngine() {
        return scriptEngine;
    }
}
