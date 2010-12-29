package com.agimatec.tools.nls.model;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:22:00 <br/>
 * Copyright: Agimatec GmbH
 */
//@XStreamAlias("text")
public class MBText implements Comparable {
//    @XStreamAsAttribute
    private String locale;
    private String value;
//   @XStreamAsAttribute    
    private boolean review;
    private boolean useDefault;

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

    public boolean isReview() {
        return review;
    }

    public void setReview(boolean review) {
        this.review = review;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }

    public int compareTo(Object o) {
        MBText other = (MBText) o;
        if (locale == null) return 0;
        int dif = locale.compareTo(other.getLocale());
        if (dif == 0 && value != null) {
            return value.compareTo(other.getValue());
        } else {
            return dif;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MBText mbText = (MBText) o;

        return review == mbText.review && useDefault == mbText.useDefault &&
                !(locale != null ? !locale.equals(mbText.locale) : mbText.locale != null) &&
                !(value != null ? !value.equals(mbText.value) : mbText.value != null);
    }
}
