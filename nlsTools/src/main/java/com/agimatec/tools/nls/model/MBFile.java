package com.agimatec.tools.nls.model;

/**
 * Description: <br/>
 * User: roman <br/>
 * Date: 10.02.2009 <br/>
 * Time: 14:14:11 <br/>
 * Copyright: Agimatec GmbH
 */
public final class MBFile {
    private String name;
    private String content;
    private long lastModified;

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setName(String fileName) {
        name = fileName;
    }

    public void setContent(String s) {
        content = s;
    }

    public void setLastModified(long time) {
        this.lastModified = time;
    }
}