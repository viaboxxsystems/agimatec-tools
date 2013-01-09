package com.agimatec.commons.config;

import com.agimatec.commons.config.sax.ConfigContentHandler;
import com.agimatec.commons.config.sax.ConfigWriter;
import com.agimatec.commons.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Provides central access to configuration files.
 * The singleton can be used (getDefault()).
 * A ConfigManger uses a resource name (file) in the classpath that contains
 * the absolute config root path.
 * If the resource cannot be found, the config manager tries
 * to access a system property with the same name.
 * If such a system property exists,
 * its value is used as the config root path.
 * The default configrootressourcename is "configroot.ini".
 * <br>
 * ConfigManager.getDefault() sucht eine Resource configroot.ini im Klassenpfad.
 * Es koennen andere ConfigManager instanziert werden, die ggf. eine andere Resource verwenden,
 * wenn man mehrere config-roots haben moechte.
 * Die configroot.ini Datei enthaelt den Pfad, in dem die Datei configroot.xml zu finden ist.
 * Wird die Resource nicht gefunden, so ist config-root das aktuelle Verzeichnis.
 * Im Config-root wird als Einstieg in die Konfigurationen die configDatei configroot.xml
 * gesucht (die muss immer so heissen).
 * In dieser Datei sind nur &lt;file> Tags erlaubt, die auf andere ConfigFiles verweisen und
 * zwar entweder relativ zum configroot (als Default) oder mit absolutem Pfad (relative="false").
 * Alle anderen Configfiles koennen beliebige Datenstrukturen darstellen:
 * - alle primitiv-Typen als Wrapperklasse oder als Node-Klasse
 * - list, map, ArrayList, HashMap
 * - filenodes auf andere Dateien, die ggf. keine config-dateien mehr sind
 * (so kann auf bel. Resourcen verwiesen werden ohne den JavaSource Pfadabh. zu machen)
 * <br>Author: Roman Stumm
 */
public class ConfigManager implements Serializable {
    private static final Logger myLogger = LoggerFactory.getLogger(ConfigManager.class);
    public static final String C_ProtocolClassPath = "cp://";
    protected static ConfigManager singleton = new ConfigManager("configmanager.ini");

    private static final String DEFAULT_CONFIGROOT_PATH = "file:";

    protected final String myConfigRootRessouceName;
    protected Config myConfigroot = null;
    protected String myConfigrootPath = null;
    protected Map myConfigMap = new Hashtable();

    /**
     * Creates a new ConfigManager.
     *
     * @param aConfigRootRessouceName the resource name (file) in the classpath that contains
     *                                the absolute config root path. If the resource cannot be found, the config manager tries
     *                                to access a system property with the same name. If such a system property exists,
     *                                its value is used as the config root path.
     *                                The default configrootressourcename is "configroot.ini".
     */
    public ConfigManager(final String aConfigRootRessouceName) {
        myConfigRootRessouceName = aConfigRootRessouceName;
    }

    /**
     * @return the singleton default instance of this class
     */
    public static ConfigManager getDefault() {
        return singleton;
    }

    /**
     * open and parse the file name given in path.
     * the file is excepted to be an xml file that
     * conforms to the config.dtd
     */
    public Config readConfig(String aPath, final boolean isRelativeFlag) {
        if (myLogger.isDebugEnabled()) {
            myLogger.debug("going to read config file (relative path): " + aPath);
        }
        if (isRelativeFlag) aPath = makeAbsolutePath(aPath);
        if (myLogger.isDebugEnabled()) {
            myLogger.debug("reading config file (path made absolute): " + aPath);
        }
        try {
            final ConfigContentHandler theConfigHandler = new ConfigContentHandler(this);
            final XMLReader theParser = this.createParser();
            theParser.setContentHandler(theConfigHandler);
            parseResource(theParser, aPath);
            return theConfigHandler.getConfig();
        } catch (Exception ex) {
            myLogger.error("cannot load xml configuration from " + aPath, ex);
            return null;
        }
    }

    public Config parseConfig(String aConfigXMLString) {
        try {
            final ConfigContentHandler theConfigHandler = new ConfigContentHandler(this);
            final XMLReader theParser = this.createParser();
            theParser.setContentHandler(theConfigHandler);
            parseString(theParser, aConfigXMLString);
            return theConfigHandler.getConfig();
        } catch (Exception ex) {
            myLogger.error(null, ex);
            return null;
        }
    }

    /**
     * parse xml in the given string
     *
     * @param aParser          - the parse to use
     * @param aConfigXMLString - the xml to parse
     * @throws IOException
     * @throws SAXException
     */
    protected void parseString(XMLReader aParser, String aConfigXMLString)
            throws IOException, SAXException {
        StringReader reader = new StringReader(aConfigXMLString);
        try {
            aParser.parse(new InputSource(reader));
        } finally {
            reader.close();
        }
    }

    /**
     * parse either from a file or the classpath
     */
    protected void parseResource(final XMLReader aParser, final String aPath)
            throws IOException, SAXException {
        if (aPath.startsWith(C_ProtocolClassPath)) {
            final String theResPath = aPath.substring(C_ProtocolClassPath.length());
            final InputStream is =
                    ClassUtils.getClassLoader().getResourceAsStream(theResPath);
            try {
                aParser.parse(new InputSource(is));
            } finally {
                if (is != null) is.close();
            }
        } else {
            aParser.parse(aPath);
        }
    }

    public static URL toURL(String path) throws MalformedURLException {
        if (path.startsWith(C_ProtocolClassPath)) {
            final String theResPath = resolvePath(path.substring(C_ProtocolClassPath.length()));
            return ClassUtils.getClassLoader().getResource(theResPath);
        } else if (path.indexOf(':') < 0) {
            return new URL("file:" + path);
        } else {
            return new URL(path);
        }
    }

    public static List<URL> toURLs(String path) throws IOException {
        if (path.startsWith(C_ProtocolClassPath)) {
            final String theResPath = resolvePath(path.substring(C_ProtocolClassPath.length()));
            Enumeration<URL> en = ClassUtils.getClassLoader().getResources(theResPath);
            List<URL> urls = new ArrayList();
            while (en.hasMoreElements()) {
                URL next = en.nextElement();
                urls.add(next);
            }
            return urls;
        } else {
            List<URL> urls = new ArrayList(1);
            urls.add(toURL(path));
            return urls;
        }
    }

    public static String resolvePath(String theResPath) {
        /**
         * in some classloaders, e.g. tomcat webapp .. cannot be part of a resource path,
         * so try to remove it
         */
        if (theResPath.contains("..")) {
            StringTokenizer tokens = new StringTokenizer(theResPath, "/", true);

            LinkedList<String> parts = new LinkedList<String>();
            boolean forward = false;
            while (tokens.hasMoreTokens()) {
                String part = tokens.nextToken();
                if(forward) {
                    forward = false;
                    if("/".equals(part)) continue;
                }
                if ("..".equals(part)) {
                    if (parts.size() > 1 && "/".equals(parts.getLast())) {
                        parts.removeLast();
                        parts.removeLast();
                        forward = true;
                    } else {
                        parts.add(part);
                    }
                } else {
                    parts.add(part);
                }
            }
            StringBuilder buf = new StringBuilder();
            for(String part : parts) {
                buf.append(part);
            }
            theResPath = buf.toString();
            return theResPath;
        } else {
            return theResPath;
        }
    }

    /**
     * initialize lazy and return the receiver's script instance
     * ready to be used.
     */
    protected XMLReader createParser() {
        return new com.sun.org.apache.xerces.internal.parsers.SAXParser();
    }

    /**
     * open and parse the file name given in path.
     * the file is excepted to be an xml file that
     * conforms to the config.dtd
     */
    public Config readConfig(final String aPath) {
        return readConfig(aPath, true);
    }

    public void writeConfig(final Config aConfig, final PrintWriter aPrintWriter)
            throws IOException {
        try {
            new ConfigWriter().writeConfig(aConfig, aPrintWriter);
        } catch (SAXException saxEx) {
            myLogger.error(null, saxEx);
        }
    }

    public String writeConfig(final Config aConfig) throws IOException {
        try {
            final StringWriter sw = new java.io.StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            writeConfig(aConfig, pw);
            return sw.toString();
        } catch (Exception ex) {
            myLogger.error(null, ex);
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * return the config with the given name from the cache.
     * if there is no such config read it and cache it.
     */
    public Config getConfig(final String aConfigname, final String aPath) {
        return getConfig(aConfigname, aPath, true);
    }

    /**
     * return the config with the given name from the cache.
     * if there is no such config read it and cache it.
     *
     * @param isRelativeFlag - is true when the path is relative to configroot, so that
     *                       the absolute path will be created. - is false, when the path should not be modified.
     */
    public Config getConfig(final String aConfigName, final String aPath, final boolean isRelativeFlag) {
        Config theConfigRoot = getCachedConfig(aConfigName);
        if (theConfigRoot == null) {
            theConfigRoot = readConfig(aPath, isRelativeFlag);
            cacheConfig(theConfigRoot, aConfigName);
        }
        return theConfigRoot;
    }

    /**
     * @return the config or null, if there is no cached one
     */
    public Config getCachedConfig(final String aConfigName) {
        return (Config) myConfigMap.get(aConfigName);
    }

    /**
     * get or read the config named configname.
     * if the config is not cached yet, read it with the help
     * of configroot.xml and cache afterwards.
     *
     * @throws IllegalArgumentException if the config is unknown
     */
    public Config getConfig(final String configname) {
        return getConfig(configname, false);
    }

    /**
     * the same as {@link this#getConfig(java.lang.String)} but the
     * config is read from file and not cached.
     *
     * @param configname
     * @return
     */
    public Config getConfigUncached(final String configname) {
        FileNode theFileNode = (FileNode) getConfigRoot().getNode(configname);
        if (theFileNode == null) {
            throw new IllegalArgumentException(configname + " is an unknown config");
        }
        return readConfig(theFileNode.getURLPath(), false);
    }

    public Config getConfig(String configname, boolean allowNullReturn) {
        Config config = getCachedConfig(configname);
        if (config != null) return config;
        final FileNode confNode = (FileNode) getConfigRoot().get(configname);
        if (confNode == null) {
            if (!allowNullReturn)
                throw new IllegalArgumentException(configname + " is an unknown config");
            return null;
        }
        config = readConfig(confNode.getURLPath(), false);
        if (config == null && allowNullReturn) return null;
        cacheConfig(config, configname);
        return config;
    }

    public Config getConfigRoot() {
        if (myConfigroot == null) {
            synchronized (ConfigManager.class) {
                if (myConfigroot == null) {
                    myConfigroot = getConfig("configroot", "configroot.xml");
                }
            }
        }
        return myConfigroot;
    }

    /**
     * put the config into the cache
     */
    public void cacheConfig(final Config aConfig, final String aConfigName) {
        if (aConfig != null) {
            aConfig.setName(aConfigName);
            myConfigMap.put(aConfigName, aConfig);
        }
    }

    /**
     * remove the config from the cache
     */
    public void uncacheConfig(final Config aConfig) {
        myConfigMap.remove(aConfig.getName());
    }

    /**
     * remove all cached configs from the cache
     */
    public void clearCache() {
        myConfigMap.clear();
    }

    private String makeAbsolutePath(final String aRelativePath) {
        final String theConfigRootPath = this.getConfigRootPath();
        final StringBuilder strbuff =
                new StringBuilder(theConfigRootPath.length() + aRelativePath.length());
        strbuff.append(theConfigRootPath);
        strbuff.append(aRelativePath);
        return strbuff.toString();
    }

    public String getConfigRootPath() {
        if (myConfigrootPath == null) {
            synchronized (ConfigManager.class) {
                if (myConfigrootPath == null) {
                    myConfigrootPath = this.readConfigrootPath();
                }
            }
        }
        return myConfigrootPath;
    }

    /**
     * directly set the config root path.
     */
    public void setConfigRootPath(final String aConfigRootPath) {
        myConfigrootPath = aConfigRootPath;
    }

    protected String readConfigrootPath() {
        return readConfigrootPath(myConfigRootRessouceName);
    }

    protected final String readConfigrootPath(final String aResourceName) {
        String path = readRessource(ConfigManager.class.getClassLoader(), aResourceName);
        return (path == null) ? DEFAULT_CONFIGROOT_PATH : path;
    }

    /**
     * @return the contents of the resource with the given resourcename
     *         loaded with the given classloader or null, if not found.
     */
    public static String readRessource(final ClassLoader aClassLoader, final String aResourceName) {
        final URL theURL = aClassLoader.getResource(aResourceName);
        if (theURL == null) {
            if (myLogger.isDebugEnabled()) {
                myLogger.debug("ConfigManager: " + aResourceName + " not found.");
            }
            // try System.property instead
            final String configRootProp = System.getProperty(aResourceName);
            if (configRootProp != null) {
                if (myLogger.isDebugEnabled()) {
                    myLogger.debug("Using System.property with value: " + configRootProp);
                }
                return configRootProp;
            }
            return null;
        }
        try {
            final InputStream is = theURL.openStream();
            final InputStreamReader isr = new InputStreamReader(is);
            try {
                final char[] cbuf = new char[250];
                int len = isr.read(cbuf);
                final StringBuilder buf = new StringBuilder(len);
                while (len > 0) {
                    buf.append(cbuf, 0, len);
                    len = isr.read(cbuf);
                }
                char c = buf.charAt(buf.length() - 1);
                // cut of line feed, space, cr, tab from the end
                if (myLogger.isDebugEnabled()) {
                    myLogger.debug("resourcename: " + aResourceName +
                            "\n  uncutted resourcepath: " + buf.toString());
                }
                while (c == '\r' || c == '\n' || c == '\t' || c == ' ') {
                    buf.deleteCharAt(buf.length() - 1);
                    c = buf.charAt(buf.length() - 1);
                }
                return buf.toString();
            } finally {
                isr.close();
                if (is != null) is.close();
            }
        } catch (IOException ioEx) {
            myLogger.error(null, ioEx);
            return null;
        }
    }

}

