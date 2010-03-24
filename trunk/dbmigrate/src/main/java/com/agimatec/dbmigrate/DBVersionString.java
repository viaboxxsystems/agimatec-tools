package com.agimatec.dbmigrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <b>Description:</b>  Class to parse a file name with dbversion at beginning of the name <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public final class DBVersionString implements Comparable {
    private static final Log log = LogFactory.getLog(DBVersionString.class);

    private final String fileName;
    private int major, minor, increment;
    private String rest;

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getIncrement() {
        return increment;
    }

    public String getRest() {
        return rest;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        int idx = rest.lastIndexOf('.');
        if (idx == -1) return null;
        return rest.substring(idx + 1);
    }


    public static DBVersionString fromString(String prefix, String v) {
        if(prefix != null && !v.startsWith(prefix)) {
            return null;
        }
        try {
            return new DBVersionString(prefix, v);
        } catch (Exception e) {
            log.error("error creating instance for " + v, e);
            return null;
        }
    }

    public static DBVersionString fromString(String v) {
       return fromString(null, v);
    }

    protected DBVersionString(String v) {
        this(null, v);
    }

    /**
     * format:
     * majorversion.minorversion.increment[_]*.*
     * majorversion, minorversion and increment are integer strings.
     * <p/>
     * minorversion, increment and _* are optional
     */
    protected DBVersionString(String prefix, String v) {
        fileName = v;
        hoellischMiesesParsen(prefix, v);
    }

    // Kotz, kotz, kotz, ... Hacking nach alter Schule! :-(
    private void hoellischMiesesParsen(String prefix, String v) {
        if (prefix != null) { // skip prefix, if any
            v = v.substring(prefix.length());
        }
        int idx1 = v.indexOf('.');
        int idx2 = v.indexOf('.', idx1 + 1);
        int control = v.indexOf('-');
        if(control == -1) control = v.indexOf('_');
        if (control != -1 && control < idx2) {
            idx2 = -1;
        }
        int idx3 = v.indexOf('-', idx2 + 1);
        if (idx3 == -1) idx3 = v.indexOf('_', idx2 + 1);
        if (idx2 != -1) {
            if (idx3 == -1) idx3 = v.indexOf('.', idx2 + 1);
        }
        if(idx1 > control && control > 0) idx1 = control;
        major = Integer.parseInt(v.substring(0, idx1));
        if (idx2 != -1) {
            minor = Integer.parseInt(v.substring(idx1 + 1, idx2));
        } else {
            if (idx3 == -1) {
                try {
                    minor = Integer.parseInt(v.substring(idx1 + 1));
                } catch (NumberFormatException ex) {
                    minor = 0;
                    idx2 = -2;
                }
            } else if(idx1+1<idx3) {
                try {
                    minor = Integer.parseInt(v.substring(idx1 + 1, idx3));
                } catch (NumberFormatException ex) {
                    minor = 0;
                }
            }
        }
        if (idx3 != -1 && idx2 > -1) {
            increment = Integer.parseInt(v.substring(idx2 + 1, idx3));
            rest = v.substring(idx3);
        } else if (idx3 != -1 && idx2 < 0) {
            increment = 0;
            rest = v.substring(idx3);
        } else if (idx2 > -1) {
            try {
                increment = Integer.parseInt(v.substring(idx2 + 1));
                rest = "";
            } catch (NumberFormatException ex) {
                increment = 0;
                rest = v.substring(idx2);
            }

        } else {
            increment = 0;
            if (idx2 != -1) {
                rest = v.substring(idx1);
            } else {
                rest = "";
            }
        }
    }

    public String toString() {
        return String.valueOf(major) + '.' + minor + '.' + increment + rest;
    }

    public boolean versionEquals(DBVersionString other) {
        return major == other.major && minor == other.minor &&
                increment == other.increment;
    }

    public boolean isLater(DBVersionString other) {
        if (other == null) return true;
        if (major == other.getMajor()) {
            if (minor == other.getMinor()) {
                return increment > other.getIncrement();
            } else return minor > other.getMinor();
        } else return major > other.getMajor();
    }

    public int compareTo(Object o) {
        DBVersionString v2 = (DBVersionString) o;

        if (getMajor() == v2.getMajor()) {
            if (getMinor() == v2.getMinor()) {
                if (getIncrement() == v2.getIncrement()) {
                    String t1 = getFileType();
                    String t2 = v2.getFileType();
                    // xml types before others
                    if ("xml".equalsIgnoreCase(t1) && !"xml".equalsIgnoreCase(t2)) {
                        return -1;
                    }
                    if ("xml".equalsIgnoreCase(t2) && !"xml".equalsIgnoreCase(t1)) {
                        return 1;
                    }
                    return getRest().compareTo(v2.getRest());
                } else {
                    return getIncrement() - v2.getIncrement();
                }
            } else {
                return getMinor() - v2.getMinor();
            }
        } else {
            return getMajor() - v2.getMajor();
        }
    }
}



