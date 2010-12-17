package com.agimatec.tools.nls;

import com.agimatec.commons.util.FileUtils;
import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 15.06.2007 <br/>
 * Time: 12:20:23 <br/>
 * Copyright: Agimatec GmbH
 */
public class BundleWriterSql extends BundleWriter {
    public BundleWriterSql(Task task, String configFile, MBBundle currentBundle, String outputPath, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    private File getSQLFile() {
        return new File(getOutputPath(), currentBundle.getSqldomain() + suffix());
    }

    protected void writeOutputFiles() throws IOException {
        String domain = currentBundle.getSqldomain();
        File file = getSQLFile();
        Writer fw = FileUtils.openFileWriterUTF8(file);
        task.log("writing statements for SQLDomain " + domain + " to: " + file,
                Project.MSG_INFO);
        try {
            fw.write(
                    "DELETE FROM NLSTEXT t WHERE EXISTS (SELECT 1 FROM NLSBUNDLE b WHERE DOMAIN='" +
                            domain + "' AND t.BUNDLEID=b.ID);\n");
            fw.write("DELETE FROM NLSBUNDLE WHERE DOMAIN='" + domain + "';\n");
            fw.write("INSERT INTO NLSBUNDLE (ID, DOMAIN) SELECT SEQ_NLSBundle.NEXTVAL,'" +
                    domain + "' FROM DUAL;\n");

            for (MBEntry theEntry : getCurrentBundle().getEntries()) {
                String name = theEntry.getKey();
                Iterator<MBText> texts = theEntry.getTexts().iterator();
                String theKey = name.replace('.', '_');
                while (texts.hasNext()) {
                    MBText theText = texts.next();
                    String lang = theText.getLocale();
                    fw.write(
                            "INSERT INTO NLSTEXT (KEY, TRANSLATED, LOCALE, BundleID) SELECT ");
                    fw.write("'" + theKey + "','" + theText.getValue() + "', '" + lang +
                            "', b.ID  FROM NLSBUNDLE b WHERE b.DOMAIN = '" + domain +
                            "';\n");
                }
            }
            fw.write("COMMIT;\n\n");
        } finally {
            fw.close();
        }
    }

    protected boolean needsNewFiles() throws FileNotFoundException {
        File outfile = getSQLFile();
        if (!outfile.exists()) {
            return true;
        }
        File infile = new File(configFile);
        if (!infile.exists()) {
            throw new FileNotFoundException(infile + " not found");
        }
        return (infile.lastModified() > outfile.lastModified());
    }

    protected String suffix() {
        return ".sql";
    }
}
