package com.agimatec.dbtransform;

import com.agimatec.commons.generator.GeneratorTool;
import com.agimatec.sql.meta.persistence.XStreamPersistencer;
import com.agimatec.sql.meta.script.DDLExpressions;
import com.agimatec.sql.script.SQLScriptParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Description: Transform (migration) scripts from one DBMS dialect to another
 * (postgres to oracle)<br/>
 * User: roman.stumm <br/>
 * Date: 17.12.2007 <br/>
 * Time: 11:57:10 <br/>
 * Copyright: Agimatec GmbH
 */
public class ScriptTransformatorTool extends GeneratorTool implements FilenameFilter {
  private static final Logger log = LoggerFactory.getLogger(ScriptTransformatorTool.class);

  private CatalogConversion catalogConversion;

  protected Object getConfig() {
    return catalogConversion;
  }

  protected void readConfig(File configFile)
      throws IOException, ClassNotFoundException {
    if (configFile != null) {
      catalogConversion =
          (CatalogConversion) new XStreamPersistencer().load(configFile);
    }
  }

  public static void main(String[] args) throws Exception {
    ScriptTransformatorTool tool = new ScriptTransformatorTool();
    tool.runMain(args, new ScriptTransformatorSettings());
  }

  public void runMain(String[] args, ScriptTransformatorSettings settings)
      throws Exception {
    super.runMain(args, settings);
    DDLExpressions all = DDLExpressions.forDbms(settings.getFromDbms());
    if (all == null) {
      throw new IllegalArgumentException(
          "unsupported source dbms: " + settings.getFromDbms());
    }
    SomeDDLExpressions expressions = new SomeDDLExpressions(all);
    expressions.addExpression("table-alter-columns");
    expressions.addExpression("drop-trigger");
    expressions.addExpression("dezign-create-table");
    expressions.addExpression("drop-table");
    expressions.addExpression("create-index");
    File fromDir = new File(settings.getFromDir());
    new File(settings.getTargetDir()).mkdirs();

    for (File fromFile : fromDir.listFiles(this)) {
      File targetFile = new File(settings.getTargetDir(), fromFile.getName());
      if (settings.isOverwrite() || !targetFile.exists()) {
        PrintWriter target = new PrintWriter(new FileWriter(targetFile));
        try {
          SQLScriptParser parser = new SQLScriptParser(log);
          ScriptTransformator transformator = new ScriptTransformator(
              expressions, target, catalogConversion, templateEngine);
          parser.iterateSQLScript(transformator, fromFile.toURI().toURL());
        } finally {
          target.close();
        }
      } else {
        log.info("-overwrite=false, skipping overwrite of: " +
            targetFile.getPath());
      }
    }
  }

  protected ScriptTransformatorSettings getSettings() {
    return (ScriptTransformatorSettings) settings;
  }

  /**
   * decide which scripts to be transformed
   */
  public boolean accept(File dir, String name) {
    return name.startsWith(getPrefix()) && name.endsWith(".sql");
  }

  public String getPrefix() {
    return getSettings().getPrefix();
  }

}
