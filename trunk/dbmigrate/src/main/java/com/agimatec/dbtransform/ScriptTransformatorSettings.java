package com.agimatec.dbtransform;

import com.agimatec.commons.generator.GeneratorSettings;

import java.io.IOException;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 17.12.2007 <br/>
 * Time: 11:59:54 <br/>
 * Copyright: Agimatec GmbH
 */
public class ScriptTransformatorSettings extends GeneratorSettings {
  private String fromDir, targetDir, fromDbms = "postgres", prefix = "up-";
  private boolean overwrite = false;

  @Override
  protected String defaultConfigFileName() {
    return "db-conversion.xml";
  }

  @Override
  public boolean parseArgs(String[] args) throws IOException, ClassNotFoundException {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("-fromDir".equalsIgnoreCase(arg)) {
        fromDir = args[++i];
      } else if ("-targetDir".equalsIgnoreCase(arg)) {
        targetDir = args[++i];
      } else if ("-fromDbms".equalsIgnoreCase(arg)) {
        fromDbms = args[++i];
      } else if ("-overwrite".equalsIgnoreCase(arg)) {
        overwrite = Boolean.parseBoolean(args[++i]);
      } else if ("-prefix".equalsIgnoreCase(arg)) {
        prefix = args[++i];
      }
    }
    return super.parseArgs(args);   // call super!
  }

  @Override
  protected void printUsage() {
    super.printUsage();
    System.out.println("\t-fromDir \t (mandatory) dir with scripts to transform, e.g. postgres/upgrade");
    System.out.println("\t-targetDir \t (mandatory) dir to write output scripts to, e.g. oracle/upgrade");
    System.out
        .println("\t-fromDbms \t (optional) the source DBMS used to parse the scripts in 'fromDir', default: postgres");
    System.out.println("\t-overwrite \t (optional) overwrite target scripts (default: false), true/false");
    System.out.println("\t-prefix \t (optional) prefix of script files (default: up-)");
  }

  @Override
  protected void checkValid() {
    if (dbms == null) {
      throw new IllegalArgumentException("no database given with option -dbms");
    }
    if (targetDir == null) {
      throw new IllegalArgumentException(
          "no targetDir given with option -targetDir");
    }
    if (fromDir == null) {
      throw new IllegalArgumentException("no fromDir given with option -fromDir");
    }
  }

  public String getFromDir() {
    return fromDir;
  }

  public String getTargetDir() {
    return targetDir;
  }

  public String getFromDbms() {
    return fromDbms;
  }

  public boolean isOverwrite() {
    return overwrite;
  }

  public String getPrefix() {
    return prefix;
  }
}
