package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBXMLPersistencer;
import org.apache.tools.ant.*;

import java.io.File;

/**
 * Fills missing keys in locale with the values found in a different locale in teh same file.
 *
 * &lt;fillLocale
 *     localeXML=&quot;filled/filled-main-default-FR.xml&quot;
 *     targetXML=&quot;complete/main-default.xml&quot;
 *     sourceLocale=&quot;en_US&quot;
 *     targetLocale=&quot;it_IT&quot;
 *     doSetReviewFlag=&quot;false&quot;
 *     fillOnlyKeysStartingWith=&quot;key_line&quot;
 *     /&gt;
 */
public class FillLocaleTask extends Task {

    private File localeXML, targetXML;
    private String sourceLocale, targetLocale;
    private String fillOnlyKeysStartingWith;
    private boolean doSetReviewFlag=true;

    @Override
    public void execute() throws BuildException {
        MBXMLPersistencer persistencer = new MBXMLPersistencer();
        try {
            MBBundles bundles = (MBBundles) persistencer.load(localeXML);
            for (MBBundle bundle : bundles.getBundles()) {
                for (MBEntry entry : bundle.getEntries()) {
                    if(fillOnlyKeysStartingWith!=null){
                        String key = entry.getKey();
                        if(!key.startsWith(fillOnlyKeysStartingWith)) continue;
                    }
                    MBText text = entry.getText(targetLocale);
                    if ("".equals(text.getValue())) {
                        MBText sourceText = entry.getText(sourceLocale);
                        if (sourceText != null) {
                            MBText newText = new MBText();
                            newText.setValue(sourceText.getValue());
                            newText.setLocale(targetLocale);
                            newText.setReview(doSetReviewFlag || text.isReview());
                            entry.getTexts().add(newText);
                            entry.getTexts().remove(text);
                        }
                    }
                }
            }
            persistencer.save(bundles, targetXML);
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

    public File getTargetXML() {
        return targetXML;
    }

    public void setTargetXML(File targetXML) {
        this.targetXML = targetXML;
    }

    public String getSourceLocale() {
        return sourceLocale;
    }

    public void setSourceLocale(String sourceLocale) {
        this.sourceLocale = sourceLocale;
    }

    public String getTargetLocale() {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale) {
        this.targetLocale = targetLocale;
    }

    public String getFillOnlyKeysStartingWith() {
        return fillOnlyKeysStartingWith;
    }

    public void setFillOnlyKeysStartingWith(String fillOnlyKeysStartingWith) {
        this.fillOnlyKeysStartingWith = fillOnlyKeysStartingWith;
    }

    public boolean isDoSetReviewFlag() {
        return doSetReviewFlag;
    }

    public void setDoSetReviewFlag(boolean doSetReviewFlag) {
        this.doSetReviewFlag = doSetReviewFlag;
    }
}
