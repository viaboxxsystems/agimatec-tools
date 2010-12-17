package com.agimatec.tools.nls;

import com.agimatec.commons.util.FileUtils;
import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 15.06.2007 <br/>
 * Time: 12:14:13 <br/>
 * Copyright: Agimatec GmbH
 */
public class BundleWriterJavaInterface extends BundleWriter {
    public BundleWriterJavaInterface(Task task, String configFile, MBBundle currentBundle, String outputPath, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }


    protected String suffix() {
        return ".java";
    }

    /**
     * generate the current bundle's interface
     *
     * @throws Exception
     */
    public void writeOutputFiles() throws Exception {
        // now write the interface
        String iffile = getInterfaceFileName();
        task.log("writing interface to: " + iffile, Project.MSG_INFO);
        mkdirs(iffile);
        Writer out = FileUtils.openFileWriterUTF8(new File(iffile));
        PrintWriter pw = new PrintWriter(out);
        try {
            writeStaticIntro(pw);
            if (FileType.JAVA_SMALL == fileType || FileType.JAVA_ENUM_KEYS == fileType) {
                pw.println(
                        "// keys not contained (small interface). see .xml source file for possible keys");
            }
            else if (FileType.JAVA_FULL == fileType || FileType.JAVA_FULL_ENUM_KEYS == fileType) {
                writeConstants(pw, getCurrentBundle());
            }
            writeStaticOutro(pw);
        } finally {
            pw.close();
        }
    }

    String getIPackage() {
        String inf = currentBundle.getInterfaceName();
        int pidx = inf.lastIndexOf('.');
        if (pidx < 0) {
            return "";
        }
        return inf.substring(0, pidx);
    }

    String getIClass() {
        String inf = currentBundle.getInterfaceName();
        int pidx = inf.lastIndexOf('.');
        if (pidx < 0) {
            return inf;
        }
        return inf.substring(pidx + 1);
    }

    private String getInterfaceFileName() {
        StringBuilder fileName = new StringBuilder(getInterfacePathName());
        fileName.append("/");
        fileName.append(getIClass());
        fileName.append(suffix());
        return fileName.toString();
    }

    private String getInterfacePathName() {
        String fileName = getOutputPath();
        char lastChar = fileName.charAt(fileName.length() - 1);
        if (lastChar != '\\' && lastChar != '/') {
            fileName += "/";
        }
        fileName += getIPackage().replace('.', '/');
        return fileName;
    }

    /**
     * Write the static beginning of the interface file.
     */
    void writeStaticIntro(PrintWriter pw) {
        String str = getIPackage();
        if (str != null && str.length() > 0) {
            pw.print("package ");
            pw.print(str);
            pw.println(";");
        }
        pw.println();
        writeDoNotAlter(pw);
        pw.print("public interface ");
        pw.print(getIClass());
        pw.println(" {");
        pw.print("  String _BUNDLE_NAME = \"");
        pw.print(currentBundle.getBaseName());
        pw.println("\";");
        pw.println();
    }

    void writeDoNotAlter(PrintWriter pw) {
        pw.println("/**");
        pw.print(" * contains keys of resource bundle ");
        pw.print(currentBundle.getBaseName());
        pw.println('.');
        pw.println(" * THIS FILE HAS BEEN GENERATED AUTOMATICALLY - DO NOT ALTER!");
        pw.println(" */");
    }

    /**
     * Write the staic end of the interface file.
     */
    private void writeStaticOutro(PrintWriter pw) {
        pw.println("}");
        writeDoNotAlter(pw);
    }

    /**
     * Write the constants to the interface file.
     */
    void writeConstants(PrintWriter pw, MBBundle aBundle) {
        Iterator<MBEntry> iter = aBundle.getEntries().iterator();
        boolean enumerateNames =
                (fileType == FileType.JAVA_ENUM_KEYS || fileType == FileType.JAVA_FULL_ENUM_KEYS); 
        List allNames = enumerateNames ? new ArrayList() : null;
        while (iter.hasNext()) {
            MBEntry eachEntry = iter.next();
            String keyName = eachEntry.getKey();
            Iterator<MBText> texts = eachEntry.getTexts().iterator();
            String value = eachEntry.getTexts().get(0).getValue();
            if (value != null) {
                pw.print("  /** ");
                while (texts.hasNext()) {
                    MBText theText = texts.next();
                    String lang = theText.getLocale();
                    pw.print("{");
                    pw.print(lang);
                    pw.print("} ");
                }
                pw.print("=> ");
                pw.print(StringEscapeUtils.escapeXml(value));
                pw.println(" */");
            }
            pw.print("  String ");
            String theKey = keyName.replace('.', '_');
            pw.print(theKey);
            if (enumerateNames) {
                allNames.add(theKey);
            }
            pw.print(" = \"");
            pw.print(keyName);
            pw.println("\";");
        }
        if (enumerateNames) {
            writeNameEnumeration(pw, allNames);
        }
    }

    /**
     * Add an array of constant names to the interface for quick enumeration purposes.
     */
    private void writeNameEnumeration(PrintWriter pw, List allNames) {
        pw.print("  String[] _ALL_KEYS = {");
        for (Iterator i = allNames.iterator(); i.hasNext();) {
            pw.print(i.next());
            if (i.hasNext()) {
                pw.print(", ");
            }
        }
        pw.println("};");
    }

    /**
     * true when generation is neccessary, false when up-to-date
     *
     * @throws java.io.FileNotFoundException
     */
    @Override
    protected boolean needsNewFiles() throws FileNotFoundException {
        File outfile = new File(getInterfaceFileName());
        if (!outfile.exists()) {
            return true;
        }
        File infile = new File(configFile);
        if (!infile.exists()) {
            throw new FileNotFoundException(infile + " not found");
        }
        return (infile.lastModified() > outfile.lastModified());
    }

}
