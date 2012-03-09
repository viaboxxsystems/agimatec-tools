package com.agimatec.commons.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileReader;
import java.io.IOException;

/**
 * Description:  a config entry representing a reference to a file or directory.
 * Copyright:    Copyright (c) 2001-2007
 *
 * @author Roman Stumm
 */
public class FileNode extends Node {
    private static final Log log = LogFactory.getLog(FileNode.class);
    protected String dir;
    protected String file;
    protected boolean relative;
    protected ConfigManager myConfigManager;

    public FileNode(final ConfigManager theConfigManager) {
        relative = true;
        myConfigManager = theConfigManager;
    }

    public Object getObjectValue() {
        return getFile();
    }

    public void setRelative(final boolean aRelative) {
        this.relative = aRelative;
    }

    /**
     * return if the file name/path is relative to cfgroot (true) or
     * absolute (false).
     * true is the default.
     */
    public boolean getRelative() {
        return relative;
    }

    /**
     * @return the config root path
     */
    public String getConfigRootPath() {

        if (log.isDebugEnabled() && myConfigManager == null) {
            log.debug("FileNode: No ConfigManager, using default");
        }
        return (myConfigManager == null) ?
                ConfigManager.getDefault().getConfigRootPath() :
                myConfigManager.getConfigRootPath();
    }

    /**
     * @return the complete path+filename
     */
    public String getURLPath() {
        final StringBuilder path = new StringBuilder();
        if (getRelative()) {
            path.append(getConfigRootPath());
        }
        if (getDir() != null) {
            path.append(getDir());
            final char lastChar = getDir().charAt(getDir().length() - 1);
            if (lastChar != '/' && lastChar != '\\' && lastChar != ':') {
                path.append('/');
            }
        }
        path.append(getFileName());
        return path.toString();
    }

    /**
     *
     * @return
     */
    public String getFilePath() {
        String p = getURLPath();
        if(p.toLowerCase().startsWith("file:")) {
            return p.substring(5);
        } else {
            return p;
        }
    }

    public String getDir() {
        return dir;
    }

    public void setDir(final String value) {
        dir = value;
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String value) {
        file = value;
    }

    public String getFileContentString() throws IOException {
        final FileReader fr = new FileReader(getURLPath());
        try {
            final char[] cbuf = new char[4096];
            final StringBuilder strbuf = new StringBuilder(256);
            int read;
            while ((read = fr.read(cbuf)) > -1) {
                strbuf.append(cbuf, 0, read);
            }
            return strbuf.toString();
        } finally {
            fr.close();
        }
    }

    /**
     * @return the filename as it is meant by the <file> tag
     */
    public String getFileName() {
        if (getFile() != null) {
            return getFile();
        } else {
            return getName() + ".xml";
        }
    }

}

