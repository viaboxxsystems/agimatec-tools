package com.agimatec.dbmigrate.util;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Description: describes the name of table and columns to store the version in the
 * database.<br/>
 * User: roman.stumm <br/>
 * Date: 13.04.2007 <br/>
 * Time: 10:31:21 <br/>
 */
public class DBVersionMeta implements Serializable {
  private String tableName = "DB_VERSION";   // mandatory
  private String column_version = "version"; // mandatory
  private String column_since = "since";   // optional
  // optional: when true, version will always be inserted (journalling)

  private boolean insertOnly = false;
  /**
   * true: create db_version table if absent
   * false: do not create, requires manual setup
   */
  private boolean autoCreate = true;
  /**
   * true: set version automatically after a script finishes
   * false: requires call to UpdateVersionScriptVisitor to persist a new db_version
   */
  private boolean autoVersion = false;

  private String sqlInsert, sqlSelect, sqlUpdate; // cache

  public boolean isAutoCreate() {
    return autoCreate;
  }

  public void setAutoCreate(boolean autoCreate) {
    this.autoCreate = autoCreate;
  }

  public boolean isAutoVersion() {
    return autoVersion;
  }

  public void setAutoVersion(boolean autoVersion) {
    this.autoVersion = autoVersion;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
    resetCache();
  }

  public String getColumn_version() {
    return column_version;
  }

  public void setColumn_version(String column_version) {
    this.column_version = column_version;
    resetCache();
  }

  protected void resetCache() {
    sqlInsert = null;
    sqlSelect = null;
    sqlUpdate = null;
  }

  /**
   * @return column name or null
   */
  public String getColumn_since() {
    return column_since;
  }

  public void setColumn_since(String column_since) {
    this.column_since = column_since;
    resetCache();
  }

  public String getQualifiedVersionColumn() {
    return tableName + "." + column_version;
  }

  public String toSQLInsert() {
    if (sqlInsert == null) {
      StringBuilder sql = new StringBuilder();
      sql.append("INSERT INTO ");
      sql.append(getTableName());
      sql.append("(").append(getColumn_version());
      if (StringUtils.isNotEmpty(getColumn_since())) {
        sql.append(", ");
        sql.append(getColumn_since());
      }
      sql.append(") VALUES(?");
      if (StringUtils.isNotEmpty(getColumn_since())) {
        sql.append(",?");
      }
      sql.append(")");
      sqlInsert = sql.toString();
    }
    return sqlInsert;
  }

  public String toSQLUpdate() {
    if (sqlUpdate == null) {
      StringBuilder sql = new StringBuilder();
      sql.append("UPDATE ");
      sql.append(getTableName());
      sql.append(" SET ");
      sql.append(getColumn_version());
      sql.append("=?");
      if (StringUtils.isNotEmpty(getColumn_since())) {
        sql.append(", ");
        sql.append(getColumn_since());
        sql.append("=?");
      }
      sqlUpdate = sql.toString();
    }
    return sqlUpdate;
  }

  public String toSQLCreateTable() {
    StringBuilder buf = new StringBuilder();
    buf.append("CREATE TABLE ").append(getTableName()).append(" (");
    if (StringUtils.isNotEmpty(getColumn_since())) {
      buf.append(getColumn_since()).append(" TIMESTAMP, ");
    }
    buf.append(getColumn_version()).append(" VARCHAR(100) NOT NULL)");
    return buf.toString();
  }

  /**
   * SQL-Statement zum Holen der neusten (=zuletzt gespeicherten) Version
   */
  public String toSQLSelectVersion() {
    if (sqlSelect == null) {
      sqlSelect = "SELECT " + getColumn_version() + " FROM " + getTableName();
      if (StringUtils.isNotEmpty(getColumn_since())) {
        sqlSelect = sqlSelect + " ORDER BY " + getColumn_since() + " DESC";
      }
    }
    return sqlSelect;
  }

  public void setInsertOnly(boolean aBoolean) {
    insertOnly = aBoolean;
  }

  public boolean isInsertOnly() {
    return insertOnly;
  }
}
