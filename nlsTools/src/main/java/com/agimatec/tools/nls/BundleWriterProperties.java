package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 15.06.2007 <br/>
 * Time: 09:44:05 <br/>
 * Copyright: Agimatec GmbH
 */
public class BundleWriterProperties extends BundleWriter {
    public BundleWriterProperties(Task task, String configFile, MBBundle currentBundle, String outputPath, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType,allowedLocales);
    }

    protected String suffix() {
        return fileType == FileType.XML ? ".xml" : ".properties";
    }

    protected String getPropertiesHeader(String locale) {
        return " THIS FILE HAS BEEN GENERATED AUTOMATICALLY - DO NOT ALTER!\r\n" +
                "#\r\n" + "# resource bundle: " + getCurrentBundle().getBaseName() +
                "\r\n" + "# locale: " + locale + "\r\n" + "# interface: " +
                getCurrentBundle().getInterfaceName() + "\r\n" + "#";
    }

    protected void writeOutputFilePerLocale(String locale)
            throws IOException {
        String propfile = getFileName(locale);
        task.log("writing resource file " + propfile, Project.MSG_INFO);
        mkdirs(propfile);
        FileOutputStream stream = new FileOutputStream(propfile);
        try {
            String header = getPropertiesHeader(locale);
            writeProperties(stream, locale, header);
        } finally {
            stream.close();
        }
    }

    protected void writeProperties(OutputStream stream, String aLocale, String header)
            throws IOException {
        Properties p = createProperties(aLocale);
        if (fileType.equals(FileType.XML)) {
            p.storeToXML(stream, header);
        } else {
            p.store(stream, header);
        }
    }
}
