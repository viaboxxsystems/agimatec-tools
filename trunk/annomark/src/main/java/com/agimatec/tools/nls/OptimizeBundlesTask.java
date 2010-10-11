package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBXMLPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * Description: analyze nls-texts in bundle files (mentioned in 'masterFile')
 * and remove all texts that have an equal common translation.
 * This reduces the bundles by removing redundant translations.
 * <p/>
 * masterFile = file with file names to analyze/optimize.
 * The task will update each file in 'masterFile'.
 * <p/>
 * <br/>
 * example:
 * <pre>
 * &lt;taskdef name="optbundles"
 *      classname="com.agimatec.tools.nls.OptimizeBundlesTask"&gt;
 *   &lt;classpath refid="maven.test.classpath"/&gt;
 * &lt;/taskdef&gt;
 * &lt;optbundles masterFile="allBundles.txt"
 *   commonBundleFile="Common.xml"
 *   commonBundleBaseName="/Common"
 *   commonEntryKeyPrefix="common"
 * /&gt;
 * <p/>
 * Transformation examples:
 * order.address.city => common.city
 * customer.firstName => common.firstName
 * <p/>
 * Removes the specific text from the bundles, where the common text is equal.
 * If "commonEntryKeyPrefix" not set, replace equal keys in bundles.
 * If "deleteEmptyEntries" is true, remove text and entries without equal translations in bundles.
 * <p/>
 * ------------------------------
 * file allBundles.txt (example):
 * common.xml
 * customer.xml
 * orders.xml
 * ------------------------------
 * </pre>
 * <br/>
 * User: roman <br/>
 * Date: 08.10.2009 <br/>
 * Time: 14:07:20 <br/>
 * Copyright: Agimatec GmbH
 */
public class OptimizeBundlesTask extends Task {
    private static final String DEFAULT_COMMON_BUNDLE_FILE = "Common.xml";
    private static final String DEFAULT_COMMON_BASE_NAME = "/Common";

    private File masterFile;

    private boolean deleteEmptyEntries = false;
    private String commonBundleFile = DEFAULT_COMMON_BUNDLE_FILE;
    private String commonBundleBaseName =
            DEFAULT_COMMON_BASE_NAME; // <bundle baseName=""/>
    private String commonEntryKeyPrefix = null; // no prefix

    public boolean isDeleteEmptyEntries() {
        return deleteEmptyEntries;
    }

    public void setDeleteEmptyEntries(boolean deleteEmptyEntries) {
        this.deleteEmptyEntries = deleteEmptyEntries;
    }

    public File getMasterFile() {
        return masterFile;
    }

    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    public String getCommonBundleFile() {
        return commonBundleFile;
    }

    public void setCommonBundleFile(String commonBundleFile) {
        this.commonBundleFile = commonBundleFile;
    }

    public String getCommonBundleBaseName() {
        return commonBundleBaseName;
    }

    public void setCommonBundleBaseName(String commonBundleBaseName) {
        this.commonBundleBaseName = commonBundleBaseName;
    }

    public String getCommonEntryKeyPrefix() {
        return commonEntryKeyPrefix;
    }

    public void setCommonEntryKeyPrefix(String commonEntryKeyPrefix) {
        this.commonEntryKeyPrefix = commonEntryKeyPrefix;
    }

    public void execute() {

        if (masterFile == null) throw new BuildException("masterFile required");
        try {
            Map<String, File> mappings =
                    CopyBundlesTask.readControlFileMapping(masterFile);
            MBBundle commonBundle = loadCommonBundle(mappings);
            MBXMLPersistencer persistencer = new MBXMLPersistencer();
            boolean modified;

            for (Map.Entry<String, File> fileentry : mappings.entrySet()) {
                File source = fileentry.getValue();
                MBBundles bundles = (MBBundles) persistencer.load(source);
                modified = false;
                for (MBBundle bundle : bundles.getBundles()) {
                    if (fileentry.getKey().equals(getCommonBundleFile()) &&
                            bundle.getBaseName().equals(getCommonBundleBaseName()))
                        continue;
                    for (MBEntry entry : bundle.getEntries()) {
                        MBEntry commonEntry = findDefaultEntry(entry, commonBundle);
                        if (commonEntry != null) {
                            for (MBText mbText : entry.getTexts()) {
                                MBText text = mbText;
                                MBText commonText = commonEntry.getText(text.getLocale());
                                if (text.getValue() != null &&
                                        text.getValue().length() > 0 &&
                                        commonText != null &&
                                        commonText.getValue() != null &&
                                        commonText.getValue().equals(text.getValue())) {
                                    this.log("Using default at " + source + ": " + entry.getKey() +
                                            "[" + text.getLocale() + "]");
                                    modified = true;
                                    text.setValue(null);
                                    text.setUseDefault(true);
                                }
                            }
                        }
                    }
                }
                if (modified) {
                    if(isDeleteEmptyEntries()) {
                        truncateBundles(bundles);
                    }
                    this.log("SAVING modified file: " + source);
                    persistencer.save(bundles, source);
                }
            }
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    private void truncateBundles(MBBundles bundles) {
        for(MBBundle bundle : bundles.getBundles()) {
            for (Iterator<MBEntry> iterator1 = bundle.getEntries().iterator(); iterator1.hasNext();) {
                MBEntry entry = iterator1.next();
                boolean isEntryEmpty = true;
                for (Iterator<MBText> iterator = entry.getTexts().iterator(); iterator.hasNext();) {
                    MBText text = iterator.next();
                    if (text.getValue() == null || text.getValue().length() == 0) {
                        iterator.remove();
                        this.log("Removed empty text " + entry.getKey() + ", locale: " + text.getLocale());
                    }
                }
                if (entry.getTexts().isEmpty()) {
                    iterator1.remove();
                      this.log("Removed empty entry " + entry.getKey());
                }
            }
        }
    }

    private MBEntry findDefaultEntry(MBEntry nlsEntry, MBBundle commonBundle) {
        MBEntry commonEntry = null;
        if (nlsEntry.getKey() == null) return null;
        String nlsKey = nlsEntry.getKey();
        if (commonEntryKeyPrefix == null) {
            commonEntry = commonBundle.getEntry(nlsKey);
        } else {
            int idx = nlsKey.lastIndexOf('.');
            if (idx >= 0) { // no prefix
                String commonKey = getCommonEntryKeyPrefix() + nlsKey.substring(idx);
                commonEntry = commonBundle.getEntry(commonKey);
            }
        }
        return commonEntry;
    }

    private MBBundle loadCommonBundle(Map<String, File> mappings)
            throws IOException, ClassNotFoundException {
        MBXMLPersistencer persistencer = new MBXMLPersistencer();
        File commonFile = mappings.get(getCommonBundleFile());
        if (commonFile == null)
            throw new BuildException(getCommonBundleFile() + " not found in " + mappings);

        MBBundles commonBundles = (MBBundles) persistencer.load(commonFile);
        MBBundle commonBundle = commonBundles.getBundle(getCommonBundleBaseName());
        if (commonBundle == null) throw new BuildException(
                getCommonBundleBaseName() + " not found in " + commonFile);
        return commonBundle;
    }
}
