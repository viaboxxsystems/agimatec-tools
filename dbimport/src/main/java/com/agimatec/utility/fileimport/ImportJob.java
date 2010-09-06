package com.agimatec.utility.fileimport;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Job definition of a dbimport job.
 * This class can be used to simplify the usage of the APIs of dbimport.
 * User: roman.stumm
 * Date: 09.04.2010<br>
 * Time: 14:06:29<br>
 * viaboxx GmbH, 2010
 *
 * @see ImportController
 * @see ImportControl
 * @see Importer
 * @see ImporterSpec
 */
public class ImportJob {
  public static int MAX_LENGTH = 1000;

  protected static final String C_ProtocolClassPath = "cp://"; // avoid dependency to dbmigrate

  protected final ImportControl control = new ImportControl();
  protected Connection connection;
  protected ImportController controller;

  protected Importer importer;
  protected InputStream readStream;

  public ImportJob() {
  }

  public ImportJob(Connection connection) {
    this.connection = connection;
  }

  /**
   * utility method -
   * from the resource string create URL.
   *
   * @param path - cp:// for class path resource or and other protocol. if no protocol is given, assume file: protocol
   * @return the URL to load the resource or null if not found
   * @throws MalformedURLException
   */
  public static URL toURL(String path) throws MalformedURLException {
    if (path.startsWith(C_ProtocolClassPath)) {
      final String theResPath = path.substring(C_ProtocolClassPath.length());
      return getClassLoader().getResource(theResPath);
    } else if (path.indexOf(':') < 0) {
      return new URL("file:" + path);
    } else {
      return new URL(path);
    }
  }

  private static ClassLoader getClassLoader() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) loader = ImportJob.class.getClassLoader();
    return loader;
  }

  /**
   * API -
   * start the import, close all resources afterwards
   */
  public void importFromURL(ImporterSpec spec) throws Exception {
    importFromReader(spec, "UTF-8");
  }

  /**
   * API -
   * start the import, close all resources afterwards
   */
  public void importFromStream(ImporterSpec spec) throws Exception {
    openURL(spec);
    try {
      importer.importFrom(readStream);
    } catch (Exception ex) {
      handleException(ex);
    } finally {
      close();
    }
  }

  /**
   * API -
   * start the import, close all resources afterwards
   */
  public void importFromReader(ImporterSpec spec, String charset) throws Exception {
    openURL(spec);
    try {
      importer.importFrom(new InputStreamReader(readStream, Charset.forName(charset)));
    } catch (Exception ex) {
      handleException(ex);
    } finally {
      close();
    }
  }

  protected void handleException(Exception ex) throws Exception {
    handleErrorMessage(ex);
    throw ex;
  }

  protected void handleErrorMessage(Exception ex) {
    control.setErrorMessage(ex.getMessage());
    if (control.getErrorMessage().length() > MAX_LENGTH)
    control.setErrorMessage(control.getErrorMessage().substring(0, MAX_LENGTH));
  }

  protected void openURL(ImporterSpec spec) throws IOException {
    importer = new Importer(spec);
    readStream = toURL(control.getFileName()).openStream();
  }

  protected void close() throws SQLException, IOException {
    if (importer != null && controller != null) controller.end(control, importer);
    if (readStream != null) readStream.close();
    if (connection != null) connection.close();
  }

  /**
   * API -
   * wait for other imports of the same kind.
   * requires a controller being set before calling this method.
   *
   * @throws SQLException
   */
  public void join() throws SQLException {
    controller.join(control);
  }

  public Connection getConnection() {
    return connection;
  }

  /**
   * set a connection before the import starts
   *
   * @param connection
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public ImportController getController() {
    return controller;
  }

  /**
   * never null, but optional
   *
   * @return
   */
  public ImportControl getControl() {
    return control;
  }

  public Importer getImporter() {
    return importer;
  }

  /**
   * optional: set the controller for the import
   *
   * @param controller
   */
  public void setController(ImportController controller) {
    this.controller = controller;
  }

  /**
   * need not call this
   *
   * @param importer
   */
  public void setImporter(Importer importer) {
    this.importer = importer;
  }
}
