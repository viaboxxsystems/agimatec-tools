package com.agimatec.dbmigrate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * <b>Description:</b>  Class to parse a file name with dbversion at beginning of the name <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public final class DBVersionString implements Comparable {
  private static final Logger log = LoggerFactory.getLogger(DBVersionString.class);

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
    if (prefix != null && !v.startsWith(prefix)) {
      return null;
    }
    try {
      return new DBVersionString(prefix, v);
    } catch (Exception e) {
      log.warn("error creating instance for " + v + " because " + e.getMessage());
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
    parse(prefix, v);
  }

  private static final String SEPS = ".-_";

  private void parse(String prefix, String v) {
    major = 0;
    minor = 0;
    increment = 0;
    if (prefix != null) { // skip prefix, if any
      v = v.substring(prefix.length());
    }

    StringTokenizer tokens = new StringTokenizer(v, SEPS, true);
    String t = nextNonSep(tokens);
    if (t != null && StringUtils.isNumeric(t)) {
      major = Integer.parseInt(t);
      t = nextNonSep(tokens);
      if (t != null && StringUtils.isNumeric(t)) {
        minor = Integer.parseInt(t);
        t = nextNonSep(tokens);
        if (t != null && StringUtils.isNumeric(t)) {
          increment = Integer.parseInt(t);
          t = null;
          lastSep = null;
        }
      }
    } else {
      throw new NumberFormatException("'" + v + "' is not in the valid format for a version");
    }

    if (tokens.hasMoreTokens() || t != null) {
      StringBuilder buf = new StringBuilder();
      if (lastSep != null) buf.append(lastSep);
      if (t != null) buf.append(t);
      while (tokens.hasMoreTokens()) {
        buf.append(tokens.nextToken());
      }
      rest = buf.toString();
    } else {
      rest = "";
    }
  }

  private String lastSep;

  private String nextNonSep(StringTokenizer tokens) {
    lastSep = null;
    while (tokens.hasMoreTokens()) {
      String t = tokens.nextToken();
      if (!SEPS.contains(t)) return t;
      lastSep = t;
    }
    return null;
  }

  public String toString() {
    return getVersion() + rest;
  }

  public String getVersion() {
    return String.valueOf(major) + '.' + minor + '.' + increment;
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



