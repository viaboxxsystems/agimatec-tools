package com.agimatec.annotations.jam;

import com.agimatec.commons.generator.FreemarkerFileGenerator;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jam.JClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description: Tool to generate files per class or one file for all classes
 * based on freemarker templates.
 * Input is a collection of JClass, that has been parsed with JAM.
 * <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 09:20:25 <br/>
 * Copyright: Apache 2.0 License
 */
public class JAMDtoGenerator {
    private String templateDir;
    private List<JAMGenInstruction> instructions = new ArrayList(2);
    private static final ThreadLocal<Collection<JAMDtoClass>> jamClasses =
            new ThreadLocal<Collection<JAMDtoClass>>();
    private static final ThreadLocal<JAMGenInstruction> currentInstruction =
            new ThreadLocal<JAMGenInstruction>();
    private FreemarkerFileGenerator generator;

    public String getTemplateDir() {
        return templateDir;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    /**
     * replacement for the freemarker build-in ?capitalize:
     * this creates a capatialized string according to java property standards.
     * Freemarker instead lowers all characters expect the first one.
     *
     * @param string
     * @return
     */
    public String capitalize(String string) {
        return StringUtils.capitalize(string);
    }

    public static JAMGenInstruction getCurrentInstruction() {
        return currentInstruction.get();
    }

    public static String getCurrentEntity() {
        return getCurrentInstruction().getUsageQualifier();
    }

    public static JAMDtoClass getJAMClass(String qualifiedName) {
        for (JAMDtoClass each : jamClasses.get()) {
            if (each.getName().equals(qualifiedName)) return each;
        }
        return null;
    }

    /**
     * @param templateBaseName
     * @param outputDir
     * @param outputFile
     */
    public JAMGenInstruction addInstruction(String templateBaseName,
                                                   String outputDir, String outputFile) {
        JAMGenInstruction i = new JAMGenInstruction();
        i.setTemplate(templateBaseName + ".ftl");
        i.setOutputDir(outputDir);
        i.setOutputFile(outputFile);
        i.setPrefix("");
        i.setSuffix(".java");
        instructions.add(i);
        return i;
    }

    /**
     * API - start all code generation.
     *
     * @param classes - the classes visible to the generator for processing, parsed with JAM
     * @throws IOException
     * @throws TemplateException
     */
    public void generate(Collection<JClass> classes)
            throws IOException, TemplateException {
        jamClasses.set(wrap(classes));
        // process templates for each class
        generator = new FreemarkerFileGenerator(new File(templateDir));
        generator.putModel("classes", jamClasses.get());
        generator.putModel("service", this);
        generate();

    }

    private void generate() throws IOException, TemplateException {
        // create missing target directories
        for (JAMGenInstruction instruction : instructions) {
            new File(instruction.getOutputDir()).mkdirs();
            setCurrentInstruction(instruction);
            generator.setBaseDir(instruction.getOutputDir());
            generator.setDestFileName(instruction.getOutputFile());
            generator.setTemplateName(instruction.getTemplate());
            generator.generate();
        }
    }

    private void setCurrentInstruction(JAMGenInstruction instruction) {
        currentInstruction.set(instruction);
    }

    private Collection<JAMDtoClass> wrap(Collection<JClass> classes) {
        Collection<JAMDtoClass> wrapped = new ArrayList(classes.size());
        for (JClass each : classes) {
            wrapped.add(new JAMDtoClass(each));
        }
        return wrapped;
    }
}
