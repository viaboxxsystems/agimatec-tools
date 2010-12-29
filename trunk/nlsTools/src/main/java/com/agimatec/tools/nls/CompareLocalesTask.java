package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.output.MBPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares new (e.g. changed by customer) and old (e.g. stuff without customer changes) locale files and
 * lists keys and bundles that are missing in the new translation.
 *
 * <br/>NEW (29.12.2010):<br/>
 *  * Can handle XML and Excel files.
 *
 * <br/>
 * Sample usage:
 * &lt;compareLocales
 *     originalXML=&quot;original/main-default.xml&quot;
 *     newXML=&quot;new/main-default.xml&quot;
 *     results=&quot;compare-results.txt&quot;
 *     /&gt;
 */
public class CompareLocalesTask extends Task {
    private File originalXML, newXML, results;

    @Override
    public void execute() throws BuildException {
        MBBundles originalBundles, newBundles;
        List<String> missingKeys = new ArrayList<String>();
        List<String> missingBundles = new ArrayList<String>();
        try {
            originalBundles = MBPersistencer.loadFile(originalXML);
            newBundles = MBPersistencer.loadFile(newXML);
            for (MBBundle originalBundle : originalBundles.getBundles()) {
                MBBundle newBundle = newBundles.getBundle(originalBundle.getBaseName());
                if (newBundle == null) {
                    missingBundles.add(originalBundle.getBaseName());
                } else {
                    for (MBEntry originalEntry : originalBundle.getEntries()) {
                        MBEntry newEntry = newBundle.getEntry(originalEntry.getKey());
                        if (newEntry == null) {
                            missingKeys.add(originalEntry.getKey());
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
            writer.append("# Comparison of ").append(originalXML.getAbsolutePath()).append(" (original) and ").append(newXML.getAbsolutePath()).append(" (new version)");
            if (missingBundles.size() > 0) {
                writer.append("# Missing bundles (").append(String.valueOf(missingKeys.size())).append("):\n");
                for (String missingBundle : missingBundles) {
                    writer.append(missingBundle);
                    writer.append("\n");
                }
            } else {
                writer.append("# No bundles missing.\n");
            }
            if (missingKeys.size() > 0) {
                writer.append("Missing keys (").append(String.valueOf(missingKeys.size())).append("):\n");
                for (String missingKey : missingKeys) {
                    writer.append(missingKey);
                    writer.append("\n");
                }
            } else {
                writer.append("No missing keys.\n");
            }
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public File getOriginalXML() {
        return originalXML;
    }

    public void setOriginalXML(File originalXML) {
        this.originalXML = originalXML;
    }

    public File getNewXML() {
        return newXML;
    }

    public void setNewXML(File newXML) {
        this.newXML = newXML;
    }

    public File getResults() {
        return results;
    }

    public void setResults(File results) {
        this.results = results;
    }
}
