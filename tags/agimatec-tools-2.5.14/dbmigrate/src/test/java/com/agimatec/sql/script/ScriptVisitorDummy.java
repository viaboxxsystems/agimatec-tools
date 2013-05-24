package com.agimatec.sql.script;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: Agimatec GmbH</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Agimatec GmbH </p>
 * @author Roman Stumm
 */
public class ScriptVisitorDummy implements ScriptVisitor {
  private final List statements = new ArrayList();
  private final List comments = new ArrayList();
  private int commits;
  private int rollbacks;

  public void reset()
  {
    commits=0;
    rollbacks=0;
    statements.clear();
    comments.clear();
  }

  public int visitStatement(String statement) throws SQLException {
    statements.add(statement);
    return 0;
  }

  public void visitComment(String theComment) throws SQLException {
    comments.add(theComment);
  }

  public void doCommit() throws SQLException {
    commits++;
  }

  public void doRollback() throws SQLException {
    rollbacks++;
  }

  public List getStatements() {
     return statements;
   }

  public List getComments() {
    return comments;
  }

  public int getCommits() {
    return commits;
  }

  public int getRollbacks() {
    return rollbacks;
  }
}
