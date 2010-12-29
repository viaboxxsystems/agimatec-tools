package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * <p>Description: Create a bundles-xml file fromProperty some plain property (or xml) files</p>
 *
 * @author Roman Stumm
 */
public class Property2XMLConverterTask extends Task {
    private boolean xml = false;
    private String fromProperty, toXML, locales;
    private String interfaceName = "";

    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }

    /**
     * name of the interface for writing into the xml error-sections file
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String aInterfaceName) {
        interfaceName = aInterfaceName;
    }

    /**
     * path/filename without _de_DE.properties /
     * each locale + .properties will be added to this string to find the properties source file to convert
     *
     * @return
     */
    public String getFromProperty() {
        return fromProperty;
    }

    public void setFromProperty(String aFromProperty) {
        fromProperty = aFromProperty;
    }

    /**
     * path/filename of the xml error-section file to be created
     *
     * @return
     */
    public String getToXML() {
        return toXML;
    }

    public void setToXML(String aToXML) {
        toXML = aToXML;
    }

    /**
     * all locales for which a resource bundle exists, separated by ;
     *
     * @return
     */
    public String getLocales() {
        return locales;
    }

    public void setLocales(String aLocales) {
        locales = aLocales;
    }

    public void execute() throws BuildException {
        MBBundles bundles = new MBBundles();
        MBBundle bundle = new MBBundle();
        bundles.getBundles().add(bundle);

        try {
            bundle.setInterfaceName(getInterfaceName());
            bundle.setBaseName(getInterfacePackage().replace('.', '/') + "/" +
                    getPropertyBaseName());

            StringTokenizer tokens = new StringTokenizer(getLocales(), ";");
            Map properties = new HashMap();
            Set allKeys = new HashSet();
            while (tokens.hasMoreTokens()) {
                String eachLocale = tokens.nextToken();
                if(eachLocale.equals("-")) eachLocale = "";   // - steht fuer die default-locale!!!
                Properties prop = new Properties();

                String fname = eachLocale.length()>0
                        ?getFromProperty() + "_" + eachLocale
                        :getFromProperty();

                if (!xml) {
                    prop.load(new FileInputStream(
                            fname + ".properties"));
                } else {
                    prop.loadFromXML(new FileInputStream(
                            fname + ".xml"));
                }
                properties.put(eachLocale, prop);
                allKeys.addAll(prop.keySet());
            }
            List allKeysList = new ArrayList(allKeys);
            Collections.sort(allKeysList);
            for (Object anAllKeysList : allKeysList) {
                String key = (String) anAllKeysList;
                MBEntry entry = new MBEntry();
                bundle.getEntries().add(entry);
                entry.setKey(key);
                tokens = new StringTokenizer(getLocales(), ";");
                while (tokens.hasMoreTokens()) {
                    String eachLocale = tokens.nextToken();
                    if(eachLocale.equals("-")) eachLocale = "";

                    Properties prop = (Properties) properties.get(eachLocale);
                    if (prop.containsKey(key)) {
                        MBText text = new MBText();
                        entry.getTexts().add(text);
                        text.setLocale(eachLocale);
                        text.setValue(prop.getProperty(key));
                    }
                }
            }
            MBPersistencer.saveFile(bundles, new File(getToXML()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    protected String getPropertyBaseName() {
        String fullName = getFromProperty();
        fullName = fullName.replace('\\', '/');
        int idx = fullName.lastIndexOf('/');
        if (idx < 0) return fullName;
        return fullName.substring(idx + 1);
    }

    protected String getInterfacePackage() {
        int idx = getInterfaceName().lastIndexOf('.');
        if (idx < 0) return "";
        return getInterfaceName().substring(0, idx);
    }
}
