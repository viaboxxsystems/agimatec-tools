package com.agimatec.annotations.jam;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 10:59:27 <br/>
 * Copyright: Agimatec GmbH
 */
public final class JAMGenInstruction {
    private String template, outputDir;
    private String prefix;
    private String suffix;
    private String usageQualifier;
    private String outputFile;
    private String defaultPackage;

    public String getOutputFile() {
        return outputFile;
    }

    public String getUsageQualifier() {
        return usageQualifier;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getFileEnding() {
        return suffix == null ? null : suffix.substring(suffix.lastIndexOf('.'));
    }

    public String getTemplate() {
        return template;
    }

    public JAMGenInstruction setTemplate(String template) {
        this.template = template;
        return this;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public JAMGenInstruction setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    /**
     * default prefix of each output file e.g. XFire
     * @param prefix
     * @return
     */
    public JAMGenInstruction setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    /**
     *  .java or file ending for each file
     * @param suffix
     * @return
     */
    public JAMGenInstruction setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     *  use case for the DTO annotations (multi annotation support) or null
     * @param usageQualifier
     * @return
     */
    public JAMGenInstruction setUsageQualifier(String usageQualifier) {
        this.usageQualifier = usageQualifier;
        return this;
    }

    public JAMGenInstruction setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        return this;
    }


    public String getDefaultPackage() {
        return defaultPackage;
    }

    public JAMGenInstruction setDefaultPackage(String defaultPackage) {
        this.defaultPackage = defaultPackage;
        return this;
    }
}
