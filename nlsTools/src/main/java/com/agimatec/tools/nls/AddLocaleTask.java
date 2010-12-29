package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Takes an XML bundle and adds new entries for the specified locales.
 * <br/>NEW (29.12.2010):<br/>
 *  * Can handle XML and Excel files.<br/>
 * @author Simon Tiffert
 */
public class AddLocaleTask extends Task {
    private String fromXML, toXML, locales;

    /**
     * The xml file with path name to read from
     * @return
     */
    public String getFromXML() {
        return fromXML;
    }

    public void setFromXML(String fromXML) {
        this.fromXML = fromXML;
    }

    /**
     * The xml file with the path name to write into
     * @return
     */
    public String getToXML() {
        return toXML;
    }

    public void setToXML(String toXML) {
        this.toXML = toXML;
    }

    /**
     * semicolon separated locale names
     * @return
     */
    public String getLocales() {
        return locales;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    public void execute() {
        MBBundles loadedBundles;
        log("Reading Bundles from " + fromXML, Project.MSG_INFO);
        // try to load the bundles of the file
        try {
            MBPersistencer persistencer = MBPersistencer.forFile(fromXML);    
            loadedBundles = persistencer.load(new File(fromXML));

            // if bundles exist
            if (loadedBundles != null) {
                for (MBBundle bundle : loadedBundles.getBundles()) {
                    for (MBEntry entry : bundle.getEntries()) {
                        // divide the locale string
                        StringTokenizer tokens = new StringTokenizer(locales, ";");
                        while (tokens.hasMoreTokens()) {
                            String locale = tokens.nextToken();

                            // check if the defined locale already exists
                            boolean newLocale = true;
                            for (MBText text : entry.getTexts()) {
                                if (text.getLocale().equals(locale)) {
                                    newLocale = false;
                                }
                            }

                            // don't overwrite or duplicate an existing locale
                            if (newLocale) {
                                // create a new locale entry with an empty value
                                MBText text = new MBText();
                                text.setLocale(locale);
                                text.setValue("");
                                entry.getTexts().add(text);
                            }
                        }
                    }
                }
            }

            // write the combined locales into a file
            persistencer.save(loadedBundles, new File(toXML));
            log("Writing to XML file " + toXML, Project.MSG_INFO);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
}
