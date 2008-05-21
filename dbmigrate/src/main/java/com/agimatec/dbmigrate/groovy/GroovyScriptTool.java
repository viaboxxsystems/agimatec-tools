package com.agimatec.dbmigrate.groovy;

import com.agimatec.dbmigrate.MigrationTool;
import com.agimatec.dbmigrate.MigrationToolAware;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;

/**
 * Description: invoke a groovy script with the migration tool<br/>
 * User: roman.stumm <br/>
 * Date: 13.11.2007 <br/>
 * Time: 16:09:13 <br/>
 * Copyright: Agimatec GmbH
 */
public class GroovyScriptTool implements MigrationToolAware {
    private GroovyScriptEngine scriptEngine;
    private Binding binding;

    public GroovyScriptTool() throws IOException {
        scriptEngine = new GroovyScriptEngine("groovy");
        binding = new Binding();
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
