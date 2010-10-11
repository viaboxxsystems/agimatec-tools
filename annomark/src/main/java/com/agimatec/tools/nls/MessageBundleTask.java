package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.output.MBXMLPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.StringTokenizer;

/**
 * <p>Description: Generates the interface with message constants and property files
 * (resource bundles) from the descriptions in a xml file.</p>
 * <p/>
 * <p/>
 * <br>Values for:<br>
 * <pre>
 * writeProperties = true (=.properties), false, xml (=.xml)
 * writeJson = true (=compressed .js file), false, pretty (formatted .js file)
 * writeInterface = true (keys + bundle name), false, small (bundle name only),
 *  enum (keys + bundle name + enum of all keys), small_enum (bundle name + enum of all keys)
 * <p/>
 * This utility can generate:
 * Properties: .properties, .xml
 * JSON: .js
 * SQL-Script: .sql
 * Interface: .java
 * <p/>
 * overwrite = (default false)
 * deleteOldFiles = (default true)
 * <p/>
 * debugMode = (default false) ignores translations and sets the key as label
 * <p/>
 * Configuration:
 * bundles = the XML-bundles input file(s), separated by ;
 * sourcePath = to write .java interface to
 * propertyPath = to write .properties/.xml to
 * jsonPath = to write .js to
 * jsonFile = null or the hard-coded json file name
 * sqlScriptDir = to write .sql to
 * directoryLocale=true: output format is "de_DE/path/bundle.properties"  (Flex style)
 * directoryLocale=false: output format is "path/bundle_de_DE.properties" (default, java style)
 * <p/>
 * </pre>
 * Example:
 * <pre>
 * &lt;taskdef name="msgbundle" classname="com.agimatec.tools.nls.MessageBundleTask">
 * &lt;classpath refid="maven.test.classpath"/>
 * &lt;/taskdef>
 *
 * &lt;msgbundle overwrite="true" bundles="src/main/bundles/Customer.xml;../utilities/base/src/main/bundles/Common.xml"
 * writeProperties="true"
 * writeJson="true"
 * jsonPath="src\main\webapp\js"
 * jsonFile="i18n"
 * propertyPath="src\main\webapp\WEB-INF\classes"/>
 * </pre>
 *
 * @author Roman Stumm
 */
public class MessageBundleTask extends Task {
    protected String bundles;
    protected String sourcePath = ".";
    protected String propertyPath = ".";
    protected String jsonPath = ".";
    protected String jsonFile = null;

    private String sqlScriptDir = null;

    private boolean overwrite = false, deleteOldFiles = true;
    private String writeProperties = "false";
    private String writeJson = "false";
    private String writeInterface = "false";
    private boolean debugMode = false;
    private boolean directoryLocale = false;

    private MBBundles parsedBundles;
    private String xmlConfigBundle;

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = getProject().resolveFile(jsonPath).getPath();
    }

    public boolean isDirectoryLocale() {
        return directoryLocale;
    }

    public void setDirectoryLocale(boolean directoryLocale) {
        this.directoryLocale = directoryLocale;
    }

    /**
     * if true, delete old .properties files before generating new ones.
     *
     * @param aDeleteOldFiles - (default = true)
     */
    public void setDeleteOldFiles(boolean aDeleteOldFiles) {
        deleteOldFiles = aDeleteOldFiles;
    }

    public String getWriteProperties() {
        return writeProperties;
    }

    public void setWriteProperties(String aWriteProperties) {
        writeProperties = aWriteProperties;
    }

    public String getWriteJson() {
        return writeJson;
    }

    public void setWriteJson(String writeJson) {
        this.writeJson = writeJson;
    }

    /** true when the interface file shall be generated */
    public String getWriteInterface() {
        return writeInterface;
    }

    public void setWriteInterface(String aWriteInterface) {
        writeInterface = aWriteInterface;
    }

    /** true when the files shall be generated, even if they are up-to-date */
    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean aOverwrite) {
        overwrite = aOverwrite;
    }

    /** true when the label shall be the same as the key */
    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /** the path+name of the xml files (; separated) */
    public String getBundles() {
        return bundles;
    }

    public void setBundles(String aConfigFile) {
        bundles = aConfigFile;
    }

    public void setSourcePath(String aSourcePath) {
        sourcePath = getProject().resolveFile(aSourcePath).getPath();
    }

    /** the root path to store the property files */
    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String aPropertyPath) {
        propertyPath = getProject().resolveFile(aPropertyPath).getPath();
    }

    public void execute() throws BuildException {
        try {
            for (MBBundle o : loadBundles().getBundles()) {
                handleInterface(o);
                handleProperties(o);
                handleJson(o);
                handleSql(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    private void handleSql(MBBundle o) throws Exception {
        final BundleWriter.FileType fileType;
        if (o.getSqldomain() == null) {
            fileType = BundleWriter.FileType.NO;
        } else {
            fileType = BundleWriter.FileType.SQL;
        }
        executeBundleWriter(
                new BundleWriterSql(this, getXMLConfigBundle(), o, getSqlScriptDir(), fileType));
    }

    private void handleInterface(MBBundle o) throws Exception {
        final BundleWriter.FileType fileType;
        if (getWriteInterface().equalsIgnoreCase("small")) {
            fileType = BundleWriter.FileType.JAVA_SMALL;
        } else if (getWriteInterface().equalsIgnoreCase("false")) {
            fileType = BundleWriter.FileType.NO;
        } else if (getWriteInterface().equalsIgnoreCase("small_enum")) {
            fileType = BundleWriter.FileType.JAVA_ENUM_KEYS;
        } else if (getWriteInterface().equalsIgnoreCase("enum")) {
            fileType = BundleWriter.FileType.JAVA_FULL_ENUM_KEYS;
        } else {
            fileType = BundleWriter.FileType.JAVA_FULL;
        }
        executeBundleWriter(
                new BundleWriterJavaInterface(this, getXMLConfigBundle(), o, sourcePath, fileType));
    }

    private void handleProperties(MBBundle o) throws Exception {
        BundleWriter.FileType fileType;
        if (getWriteProperties().equalsIgnoreCase("false")) {
            fileType = BundleWriter.FileType.NO;
        } else if (getWriteProperties().equalsIgnoreCase("xml")) {
            fileType = BundleWriter.FileType.XML;
        } else {
            fileType = BundleWriter.FileType.PROPERTIES;
        }
        executeBundleWriter(new BundleWriterProperties(this, getXMLConfigBundle(), o,
                getPropertyPath(), fileType));
    }

    private void handleJson(MBBundle o) throws Exception {
        BundleWriter.FileType fileType;
        if (getWriteJson().equalsIgnoreCase("false")) {
            fileType = BundleWriter.FileType.NO;
        } else if (getWriteProperties().equalsIgnoreCase("pretty")) {
            fileType = BundleWriter.FileType.JS_PRETTY;
        } else {
            fileType = BundleWriter.FileType.JS;
        }
        executeBundleWriter(new BundleWriterJson(this, getXMLConfigBundle(), o, getJsonPath(),
                getJsonFile(), fileType));
    }

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }

    private void executeBundleWriter(BundleWriter writer) throws Exception {
        writer.setDirectoryLocale(directoryLocale);
        writer.setOverwrite(overwrite);
        writer.setDeleteOldFiles(deleteOldFiles);
        writer.setDebugMode(debugMode);
        writer.execute();
    }

    /** read/parse XML file */
    protected MBBundles loadBundles() throws Exception {
        if (parsedBundles == null) {
            StringTokenizer tokens = new StringTokenizer(getBundles(), ";");
            while (tokens.hasMoreTokens()) {
                String bundlesFileName = getProject().resolveFile(tokens.nextToken()).getPath();
                log("Reading XML from " + bundlesFileName, Project.MSG_INFO);
                MBBundles loadedBundles =
                        (MBBundles) new MBXMLPersistencer().load(new File(bundlesFileName));
                if (parsedBundles == null) {
                    parsedBundles = loadedBundles;
                    xmlConfigBundle = bundlesFileName;
                } else {
                    mergeBundles(loadedBundles);
                }
            }
        }
        return parsedBundles;
    }

    private String getXMLConfigBundle() {
        return xmlConfigBundle;
    }

    private void mergeBundles(MBBundles loadedBundles) {
        log("Merge XML bundles ...", Project.MSG_VERBOSE);
        for (MBBundle bundle : loadedBundles.getBundles()) {
            // Enhancement NYI - duplettencheck
            for (MBBundle parsedBundle : parsedBundles.getBundles()) {
                parsedBundle.getEntries().addAll(bundle.getEntries());
            }
        }
    }

    public String getSqlScriptDir() {
        return sqlScriptDir;
    }

    public void setSqlScriptDir(String aSqlScriptDir) {
        sqlScriptDir = getProject().resolveFile(aSqlScriptDir).getPath();
    }
}