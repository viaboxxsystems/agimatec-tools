package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.output.MBJSONPersistencer;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.*;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 15.06.2007 <br/>
 * Time: 09:44:11 <br/>
 * Copyright: Agimatec GmbH
 */
public class BundleWriterJson extends BundleWriter {
    private final String outputFile;

    public BundleWriterJson(Task task, String configFile,
                            MBBundle currentBundle, String outputPath, String outputFile, 
                            FileType fileType) {
        super(task, configFile, currentBundle, outputPath, fileType);
        this.outputFile = outputFile;
    }

    protected void writeOutputFilePerLocale(String locale) throws Exception {
        String jsfile = getFileName(locale);
        mkdirs(jsfile);
        task.log("writing json file " + jsfile, Project.MSG_INFO);
        Properties merged = null;
        List<Locale> locales;
        if (!StringUtils.isEmpty(locale)) {
            locales = new ArrayList(LocaleUtils.localeLookupList(LocaleUtils.toLocale(locale)));
            Collections.reverse(locales);
            for (Locale loc : locales) {
                Properties p = createProperties(loc.toString());
                if (merged == null) {
                    merged = p;
                } else {
                    merged.putAll(p);
                }
            }
        } else {
            merged = createProperties(locale);
        }
        MBJSONPersistencer writer =
                new MBJSONPersistencer(fileType == FileType.JS_PRETTY);
        writer.save(merged, new File(jsfile));
    }

    @Override
    protected StringBuilder buildOutputFileNameBase() {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getOutputPath());
        fileName.append("/");
        if(outputFile == null) {
            fileName.append(getCurrentBundle().getBaseName());
        } else {
            fileName.append(outputFile);
        }
        return fileName;
    }

    protected String suffix() {
        return ".js";
    }
}
