package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.*;
import com.agimatec.tools.nls.output.MBBundlesZipper;
import com.agimatec.tools.nls.output.MBXMLPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.*;
import java.util.*;

/**
 * Description: update xml files from bundles in a zip file<br/>
 * User: roman <br/>
 * Date: 09.02.2009 <br/>
 * Time: 14:38:05 <br/>
 * Copyright: Agimatec GmbH
 */
public class UpdateBundlesTask extends Task {
    private File zipFile;
    private File masterFile;
    private String locales; // comma or blank separated list, default: ""==all

    public File getZipFile() {
        return zipFile;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public File getMasterFile() {
        return masterFile;
    }

    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    public String getLocales() {
        return locales;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    @Override
    public void execute() {
        if (zipFile == null) {
            throw new BuildException("zipFile required");
        }
        if (masterFile == null) {
            masterFile = getProject().getBaseDir();
        }
        try {
            FileInputStream fin = new FileInputStream(zipFile);
            List<MBFile> files = MBBundlesZipper.unzipFiles(fin);
            fin.close();
            updateFiles(files);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    private void updateFiles(List<MBFile> files) throws IOException {
        Map<String, File> mapping = CopyBundlesTask.readControlFileMapping(masterFile);

        Set<String> effectiveLocales = parseLocales(locales);
        for (MBFile file : files) {
            File outFile = mapping.get(file.getName()); // - ".xml"
            if (outFile == null) {
                getProject().log("skipping " + file.getName() + ", not mapped in " + masterFile
                      , Project.MSG_WARN);
                continue;
            }
            if (!outFile.exists()) { // create it
                getProject().log("creating (0) " + outFile);
                FileWriter writer = new FileWriter(outFile);
                if (effectiveLocales.isEmpty()) { // all locales: do not parse
                    getProject().log("writing (1) " + outFile);
                    writer.write(file.getContent());
                } else { // parse and filter
                    getProject().log("parsing (1)" + file.getName());
                    MBBundles newBundles = (MBBundles) MBXMLPersistencer.getXstream()
                          .fromXML(file.getContent());
                    filterBundleLocales(newBundles, effectiveLocales);
                    getProject().log("writing (2) " + outFile);
                    writer.write(MBXMLPersistencer.getXstream().toXML(newBundles));
                }
                writer.close();
                outFile.setLastModified(file.getLastModified());
            } else { // update it
                getProject().log("updating (1) " + outFile);
                FileReader reader = new FileReader(outFile);
                getProject().log("parsing (2) " + outFile);
                MBBundles oldBundles =
                      (MBBundles) MBXMLPersistencer.getXstream().fromXML(reader);
                reader.close();
                MBBundles newBundles = (MBBundles) MBXMLPersistencer.getXstream()
                      .fromXML(file.getContent());
                copyBundleLocales(newBundles, oldBundles, effectiveLocales);
                getProject().log("writing (3) " + outFile);
                FileWriter writer = new FileWriter(outFile);
                MBXMLPersistencer.getXstream().toXML(oldBundles, writer);
                writer.close();
                if (outFile.lastModified() < file.getLastModified()) {
                    outFile.setLastModified(file.getLastModified());
                }
            }
        }
    }

    private void filterBundleLocales(MBBundles bundles, Set<String> effectiveLocales) {
        if (effectiveLocales == null || effectiveLocales.isEmpty())
            return; // no filtering

        for (MBBundle bundle : bundles.getBundles()) {
            for (MBEntry entry : bundle.getEntries()) {
                for (Iterator<MBText> textIterator =
                      entry.getTexts().iterator(); textIterator.hasNext();) {
                    MBText text = textIterator.next();
                    if (!effectiveLocales.contains(text.getLocale())) {
                        textIterator.remove();
                    }
                }
            }
        }
    }

    private void copyBundleLocales(MBBundles from, MBBundles to,
                                   Set<String> effectiveLocale) {
        for (MBBundle bundle : from.getBundles()) {
            for (MBEntry entry : bundle.getEntries()) {
                for (MBText text : entry.getTexts()) {
                    if (effectiveLocale == null || effectiveLocale.isEmpty() ||
                          effectiveLocale.contains(text.getLocale())) { // copy
                        if (to == null) {
                            to = new MBBundles();
                        }
                        MBBundle toBundle = to.getBundle(bundle.getBaseName());
                        if (toBundle == null) {
                            toBundle = new MBBundle();
                            toBundle.setBaseName(bundle.getBaseName());
                            toBundle.setInterfaceName(bundle.getInterfaceName());
                            toBundle.setSqldomain(bundle.getSqldomain());
                            to.getBundles().add(toBundle);
                        }
                        MBEntry toEntry = toBundle.getEntry(entry.getKey());
                        if (toEntry == null) {
                            toEntry = new MBEntry();
                            toEntry.setDescription(entry.getDescription());
                            toEntry.setKey(entry.getKey());
                            toBundle.getEntries().add(toEntry);
                        }
                        MBText toText = toEntry.getText(text.getLocale());
                        if (toText != null) toEntry.getTexts().remove(toText);
                        toEntry.getTexts().add(text);
                        Collections.sort(toEntry.getTexts());
                    }
                }
            }
        }
    }

    private Set<String> parseLocales(String locales) {
        if (locales == null) return Collections.EMPTY_SET;
        StringTokenizer tokens = new StringTokenizer(locales, " ,");
        HashSet result = new HashSet();
        while (tokens.hasMoreTokens()) {
            result.add(tokens.nextToken());
        }
        return result;
    }
}

