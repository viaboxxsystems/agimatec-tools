package com.agimatec.commons.generator;

import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 17:57:39 <br/>
 * Copyright: Agimatec GmbH
 */
public class FreemarkerFileGenerator {
    protected static final Log log = LogFactory.getLog(FreemarkerFileGenerator.class);

    protected Configuration freemarker;
    private Map root = new HashMap();
    private String templateName;
    private String charset = null; // or "UTF-8"
    public static final String UTF8 = "UTF-8";

    private String destFileName, baseDir;
    private boolean defaultDestFile = true, defaultTemplateName = true;

    public FreemarkerFileGenerator(File templateDir) throws IOException {
        if (log.isInfoEnabled()) log.info("Setting templateDir = " + templateDir);
        freemarker = new Configuration();
        freemarker.setNumberFormat("0.######");  // prevent locale-sensitive number format
        if (templateDir != null) {
            freemarker.setDirectoryForTemplateLoading(templateDir);
        }
        putModel("generator", this);
        root.put("statics", BeansWrapper.getDefaultInstance().getStaticModels());
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        defaultTemplateName = false;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setDestFileName(String target) {
        this.destFileName = target;
        defaultDestFile = false;
    }

    public void putModel(String name, Object obj) {
        root.put(name, obj);
    }

    public Map getRoot() {
        return root;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getCharset() {
        return charset;
    }

    public String getDestFileName() {
        return destFileName;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void generate() throws IOException, TemplateException {
        applyDefaults();
        Writer out;
        if (destFileName != null) {
            out = createWriter(getDestFile());
        } else {
            out = NullWriter;
        }
        if (log.isInfoEnabled()) {
            log.info("Generating " + templateName + " --> " + destFileName);
        }
        try {
            generate(out);
        } finally {
            out.close();
        }
    }

    public void generate(Writer out) throws IOException, TemplateException {
        applyDefaults();
        Template template = freemarker.getTemplate(templateName);
        template.process(root, out);
    }

    public File getDestFile() {
        if (destFileName == null) return null;
        if (baseDir != null) {
            return new File(baseDir, destFileName);
        } else {
            return new File(destFileName);
        }
    }

    private Writer createWriter(File file) throws FileNotFoundException {
        Writer out;
        File dir = file.getParentFile();
        if (dir != null) dir.mkdirs();

        if (charset != null) {
            Charset cs = Charset.forName(charset);
            freemarker.setDefaultEncoding(charset);
            out = new OutputStreamWriter(new FileOutputStream(file), cs);
        } else {
            out = new OutputStreamWriter(new FileOutputStream(file));
        }
        return out;
    }

    /**
     * Utility method for templates that need to change the
     * output file during template processing!
     *
     * @throws IOException
     * @see #outputToNull()
     */
    public void outputToFile(String newDestFile) throws IOException {
        Environment env = Environment.getCurrentEnvironment();
        Writer former = env.getOut();
        former.flush(); // must not close, because the former writer is still
        // referenced in the stack and used to write pending 'nothings'
        destFileName = newDestFile;
        Writer current = createWriter(getDestFile());
        env.setOut(current);
    }

    /**
     * Utility method for template to write template output to 'nothing'.
     *
     * @throws IOException
     * @see #outputToFile(String)
     */
    public void outputToNull() throws IOException {
        Writer former = Environment.getCurrentEnvironment().getOut();
        former.flush(); // must not close, because the former writer is still
        // referenced in the stack and used to write pending 'nothings'
        Environment.getCurrentEnvironment().setOut(NullWriter);
    }

    /**
     * Utility method for template to write to the given writer
     * @param outWriter
     * @throws IOException
     */
    public void outputToWriter(Writer outWriter) throws IOException {
        Environment env = Environment.getCurrentEnvironment();
        Writer former = env.getOut();
        former.flush(); // must not close, because the former writer is still
        // referenced in the stack and used to write pending 'nothings'
        env.setOut(outWriter);
    }

    private void applyDefaults() {
        if (defaultTemplateName) {
            templateName = "template.ftl";
        }
        if (defaultDestFile) {
            destFileName = templateName + ".out";
        }
    }

    public Configuration getFreemarker() {
        return freemarker;
    }

    ///////// METHODS to help inside templates

    public List exeptLast(Collection coll) {
        LinkedList l = new LinkedList(coll);
        l.removeLast();
        return l;
    }

    public Object last(Collection coll) {
        LinkedList l = new LinkedList(coll);
        return l.removeLast();
    }

    public List exeptFirst(Collection coll) {
        LinkedList l = new LinkedList(coll);
        l.removeFirst();
        return l;
    }

    public Object first(Collection coll) {
        LinkedList l = new LinkedList(coll);
        return l.removeFirst();
    }

    public static final Writer NullWriter = new Writer() {
        public void write(char cbuf[], int off, int len) {
        }

        public void flush() {
        }

        public void close() {
        }
    };
}
