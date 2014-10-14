package com.agimatec.commons.generator;

import com.agimatec.sql.meta.CatalogDescription;
import com.agimatec.sql.meta.persistence.SerializerPersistencer;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: abstract superclass for a settings class of tools
 * that invoke freemarker templates for a catalog description.<br/>
 * User: roman.stumm <br/>
 * Date: 05.06.2007 <br/>
 * Time: 15:34:51 <br/>
 */
public abstract class GeneratorSettings {
    protected String configFile = defaultConfigFileName();

    protected abstract String defaultConfigFileName();

    protected String templateDir = "templates";
    protected String destDir = "target";
    protected String dbms = null;
    protected List<String> templates = new ArrayList();
    protected CatalogDescription catalog = null;
    protected boolean noOutputFile = false;
    private Map<String, String> properties = new HashMap();

    public boolean parseArgs(String[] args) throws IOException, ClassNotFoundException {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-conf".equalsIgnoreCase(arg)) {
                configFile = args[++i];
            } else if("-nconf".equalsIgnoreCase(arg)) {
                configFile = null;
            } else if("-nout".equalsIgnoreCase(arg)) {
                noOutputFile = true;
            } else if ("-help".equalsIgnoreCase(arg)) {
                printUsage();
                return false;
            } else if ("-ftldir".equalsIgnoreCase(arg)) {
                templateDir = args[++i];
            } else if ("-destdir".equalsIgnoreCase(arg)) {
                destDir = args[++i];
            } else if ("-dbms".equalsIgnoreCase(arg)) {
                if (dbms != null) {
                    throw new IllegalArgumentException(
                            "exactly one -dbms option is required!");
                }
                dbms = args[++i];
            } else if ("-ftl".equalsIgnoreCase(arg)) {
                templates.add(args[++i]);
            } else if ("-catalog".equalsIgnoreCase(arg)) {
                loadCatalog(args[++i]);
            } else if (arg.length() > 1 && arg.startsWith("+")) {
                String keyValue = arg.substring(1);
                int idx = arg.indexOf("=");
                if (idx > 0) {
                    String key = keyValue.substring(0, idx-1);
                    String value = keyValue.substring(idx);
                    properties.put(key, value);
                }
            }
        }
        checkValid();
        return true;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean isNoOutputFile() {
        return noOutputFile;
    }

    protected void checkValid() {
        if (catalog == null) {
            throw new IllegalArgumentException(
                    "no catalog description given with option -catalog");
        }
        if (dbms == null) {
            throw new IllegalArgumentException("no database given with option -dbms");
        }
        /*if (templates.isEmpty()) {
            throw new IllegalArgumentException(
                    "no templates given with option -ftl");
        }*/
    }

    protected void loadCatalog(String catalogFile)
            throws IOException, ClassNotFoundException {
        if (catalog != null) {
            throw new IllegalArgumentException(
                    "exactly one -catalog option is required!");
        }
        if (catalogFile.toLowerCase().endsWith(".xml")) {
            catalog = (CatalogDescription) new XStreamPersistencer()
                    .load(new File(catalogFile));
        } else {
            catalog = (CatalogDescription) new SerializerPersistencer()
                    .load(new File(catalogFile));
        }
    }

    protected void printUsage() {
        System.out.println("usage: java " + getClass().getName() +
                " -conf " + defaultConfigFileName() +
                " -catalog catalog.xml -ftldir templates -dbms dbms -ftl templateBaseName -destdir targetDirectory");
        System.out.println("Options:\n\t-help \t (optional) print this help");
        System.out.println(
                "\t-conf \t (optional) name of configuration file, default is " +
                        defaultConfigFileName());
        System.out.println("\t-nconf \t (optional) use NO configuration file");
        System.out.println("\t-nout \t (optional) do NOT write a default output file (let templates handle output file names)");
        System.out.println("\t-catalog \t (required) path of catalog file to read (xml or dump)");
        System.out.println(
                "\t-ftldir \t (optional) default: templates. Base directory with .ftl templates (in subdirs)");
        System.out.println(
                "\t-dbms \t (required) subdirs of ftldir with database-specific templates, e.g. oracle, postgres");
        System.out.println(
                "\t-ftl\t (required) can appear multiple times. the template base name (without .ftl suffix)");
        System.out.println(
                "\t-destdir\t (optional) default: target. Directory to write output files to.");
        System.out.println(
              "\t+key=value\tA property key and value that the template can access with ${key}");
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    public String getDestDir() {
        return destDir;
    }

    public String getDbms() {
        return dbms;
    }

    public List<String> getTemplates() {
        return templates;
    }

    public CatalogDescription getCatalog() {
        return catalog;
    }


    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public void setCatalog(CatalogDescription catalog) {
        this.catalog = catalog;
    }
}
