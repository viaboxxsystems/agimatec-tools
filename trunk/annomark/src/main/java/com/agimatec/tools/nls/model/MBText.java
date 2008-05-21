package com.agimatec.tools.nls.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:22:00 <br/>
 * Copyright: Agimatec GmbH
 */
@XStreamAlias("text")
public class MBText {
    @XStreamAsAttribute
    private String locale;
    private String value;

    public String getLocale() {
        return locale == null ? "" : locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
