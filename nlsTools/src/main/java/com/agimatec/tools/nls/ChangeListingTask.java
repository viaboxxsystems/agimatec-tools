package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <br/>NEW (29.12.2010):<br/>
 *  * Can handle XML and Excel files.<br/>
 */
public class ChangeListingTask extends Task {

    private File originalXML, newXML, result;
    private String checkedLocales;
    private boolean ignoreMissingKeys=true;

    @Override
    public void execute() throws BuildException {
        List<String> diffs = new ArrayList<String>();
        try {
            StringTokenizer locales = new StringTokenizer(checkedLocales, ";");
            while (locales.hasMoreTokens()) {
                String locale = locales.nextToken();
                log("Checking locale:"+locale);
                MBBundles originalBundles = MBPersistencer.loadFile(originalXML);
                MBBundles newBundles = MBPersistencer.loadFile(newXML);
                for (MBBundle originalBundle : originalBundles.getBundles()) {
                    for (MBEntry originalEntry : originalBundle.getEntries()) {
                        MBText newText = LocalesHelper.findMBTextForLocale(originalEntry.getKey(), locale, newBundles);
                        String originalTextValue = originalEntry.getText(locale) == null ? "" : originalEntry.getText(locale).getValue();
                        String newTextValue = newText == null ? "" : newText.getValue();
                        if (!(ignoreMissingKeys && newText==null) && !originalTextValue.equals(newTextValue)) {
                            String diffDescription = "("+locale+") key= " + originalEntry.getKey() + "- ORIGINAL= '" + originalTextValue + "' <-> ";
                            if (newText == null) {
                                diffDescription += "NEW: null";
                            } else {
                                diffDescription += "NEW: '" + newTextValue+"'";
                            }
                            log(diffDescription);
                            diffs.add(diffDescription);
                        }
                    }
                }
            }
            Writer writer = new FileWriter(result);
            for (String diff : diffs) {
                writer.append(diff).append("\n");
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

    public File getResult() {
        return result;
    }

    public void setResult(File result) {
        this.result = result;
    }

    public String getCheckedLocales() {
        return checkedLocales;
    }

    public void setCheckedLocales(String checkedLocales) {
        this.checkedLocales = checkedLocales;
    }
}
