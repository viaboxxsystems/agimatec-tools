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
            scriptEngine = new GroovyScriptEngine(new URL[]{
                    ConfigManager.toURL("cp://")}, ClassUtils.getClassLoader());
        } else {
            scriptEngine = new GroovyScriptEngine(new URL[]{
                    ConfigManager.toURL(rootDir)}, ClassUtils.getClassLoader());
        }
    }

    public GroovyScriptTool(String[] rootDirs) throws IOException {
        if (rootDirs != null) {
            URL[] urls = new URL[rootDirs.length];
            int i = 0;
            for (String each : rootDirs) {
                urls[i] = ConfigManager.toURL(each);
                i++;
            }
            scriptEngine = new GroovyScriptEngine(urls, ClassUtils.getClassLoader());
        } else {
            scriptEngine = new GroovyScriptEngine(new URL[]{
                    ConfigManager.toURL("cp://")}, ClassUtils.getClassLoader());
        }
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
