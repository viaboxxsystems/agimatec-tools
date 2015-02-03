package com.agimatec.dbtransform;

import com.agimatec.commons.generator.GeneratorSettings;

import java.io.IOException;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 05.06.2007 <br/>
 * Time: 15:26:03 <br/>
 */
public class CatalogGeneratorSettings extends GeneratorSettings {
    private String catalogFile;
    private String outputPrefix = "";
    private String outputSuffix = ".sql";

    @Override
    protected String defaultConfigFileName() {
        return "db-conversion.xml";
    }

    @Override
    public boolean parseArgs(String[] args) throws IOException, ClassNotFoundException {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-outputCatalog".equalsIgnoreCase(arg)) {
                catalogFile = args[++i];
            } else if ("-outputPrefix".equalsIgnoreCase(arg)) {
                outputPrefix = args[++i];
            } else if ("-outputSuffix".equalsIgnoreCase(arg)) {
                outputSuffix = args[++i];
            }
        }
        return super.parseArgs(args);
    }

    public String getCatalogFile() {
        return catalogFile;
    }

    public void setCatalogFile(String catalogFile) {
        this.catalogFile = catalogFile;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public void setOutputPrefix(String outputPrefix) {
        this.outputPrefix = outputPrefix;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public void setOutputSuffix(String outputSuffix) {
        this.outputSuffix = outputSuffix;
    }

    @Override
    protected void printUsage() {
        super.printUsage();
        System.out.println("\t-outputCatalog\t File to write output catalog to.");
        System.out
              .println("\t-outputPrefix\t Prefix for the File(s) written by templates.");
        System.out
              .println("\t-outputSuffix\t Suffix for the File(s) written by templates.");
    }

}
