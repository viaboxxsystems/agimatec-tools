package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBXMLPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks localisation files for obvious errors like missing translations for single entries.
 * This task is locale-specific, so using it include telling which locale to analyze.
 *
 * Usage sample:
 * &lt;sanityCheck
 *      locale=&quot;fr_FR&quot;
 *      ocaleXML=&quot;complete/main-default.xml&quot;
 *      results=&quot;sanity-check-FR.txt&quot;
 *      /&gt;
 */
public class LocaleSanityCheckerTask extends Task {

    private File localeXML, results;
    private String locale;
    private List<String> missingTranslations = new ArrayList<String>();

    @Override
    public void execute() throws BuildException {
        if(locale == null){
            throw new BuildException("locale parameter is needed!");
        }
        MBXMLPersistencer persistencer = new MBXMLPersistencer();
        MBBundles originalBundles;
        try {
            originalBundles = (MBBundles) persistencer.load(localeXML);
            for (MBBundle bundle : originalBundles.getBundles()) {
                for (MBEntry entry : bundle.getEntries()) {
                    MBText text = entry.getText(locale);
                    if (text == null) {
                        missingTranslations.add(entry.getKey());
                    } else {
                        String textForLocale = text.getValue();
                        if ("".equals(textForLocale)) {
                            missingTranslations.add(entry.getKey());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
        if (results.exists()) {
            throw new BuildException("Output file already exists:" + results.getAbsolutePath());
        }
        try {
            if (!results.createNewFile()) {
                throw new BuildException("Could not create result file:" + results.getAbsolutePath());
            }
            if (!results.canWrite()) {
                throw new BuildException("Cannot write to output file:" + results.getAbsolutePath());
            }
            Writer writer = new FileWriter(results);
            if (missingTranslations.size() > 0) {
                writer.append("# Missing translations (").append(String.valueOf(missingTranslations.size())).append("):\n");
                for (String missingBundle : missingTranslations) {
                    writer.append(missingBundle);
                    writer.append("\n");
                }
            } else {
                writer.append("# No translations missing.");
            }
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }

    }

    public File getLocaleXML() {
        return localeXML;
    }

    public void setLocaleXML(File localeXML) {
        this.localeXML = localeXML;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public File getResults() {
        return results;
    }

    public void setResults(File results) {
        this.results = results;
    }
}
