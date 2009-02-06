package com.agimatec.tools.nls.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:21:57 <br/>
 * Copyright: Agimatec GmbH
 */
@XStreamAlias("entry")
public class MBEntry {
    @XStreamAsAttribute
    private String key;
    private String description;  // comment field
    @XStreamImplicit(itemFieldName = "text")
    private List<MBText> texts = new ArrayList();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MBText> getTexts() {
        return texts;
    }

    public void setTexts(List<MBText> texts) {
        this.texts = texts;
    }

    public MBText getText(String locale) {
        for (MBText text : getTexts()) {
            if (text.getLocale().equals(locale)) {
                return text;
            }
        }
        return null;
    }
}
