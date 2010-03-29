package com.agimatec.commons.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.03.2010<br>
 * Time: 11:23:03<br>
 * viaboxx GmbH, 2010
 */
public class ResourceUtils {

  public static List<String> readLines(String path) throws IOException {
    ClassLoader cl = ClassUtils.class.getClassLoader();
    List<String> lines = new ArrayList();
    Enumeration<URL> e = cl.getResources(path);
    while (e.hasMoreElements()) {
      URL each = e.nextElement();
      lines.addAll(readLines(each));
    }
    return lines;
  }

  /**
   * Reads the URL contents into a list, with one element for each line.
   *
   * @param url a URL
   * @return a List of lines
   * @throws IOException if an IOException occurs.
   * @since 1.6.8
   */
  public static List<String> readLines(URL url) throws IOException {
    return readLines(newReader(url), url);
  }

  /**
   * Reads the reader into a list of Strings, with one entry for each line.
   * The reader is closed before this method returns.
   *
   * @param reader a Reader
   * @return a List of lines
   * @throws IOException if an IOException occurs.
   * @since 1.0
   */
  public static List<String> readLines(BufferedReader reader, URL url) throws IOException {
    List<String> lines = new ArrayList();
    eachLine(reader, lines, url);
    return lines;
  }

  /**
   * Creates a buffered reader for this URL.
   *
   * @param url a URL
   * @return a BufferedReader for the URL
   * @throws MalformedURLException is thrown if the URL is not well formed
   * @throws IOException           if an I/O error occurs while creating the input stream
   * @since 1.5.5
   */
  public static BufferedReader newReader(URL url) throws IOException {
    return newReader(url.openConnection().getInputStream());
  }

  /**
   * Creates a reader for this input stream.
   *
   * @param self an input stream
   * @return a reader
   * @since 1.0
   */
  public static BufferedReader newReader(InputStream self) {
    return new BufferedReader(new InputStreamReader(self));
  }

  /**
   * Iterates through the given reader line by line.  Each line is passed to the
   * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
   * as the second argument. The Reader is closed before this method returns.
   *
   * @param self a Reader, closed after the method returns
   * @return the last value returned by the closure
   * @throws IOException if an IOException occurs.
   * @since 1.5.6
   */
  public static Object eachLine(BufferedReader self, List<String> lines, URL url) throws IOException {
    return eachLine(self, 1, lines, url);
  }

  /**
   * Iterates through the given reader line by line.  Each line is passed to the
   * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
   * as the second argument. The Reader is closed before this method returns.
   *
   * @param br        a Reader, closed after the method returns
   * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
   * @return the last value returned by the closure
   * @throws IOException if an IOException occurs.
   * @since 1.5.7
   */
  public static Object eachLine(BufferedReader br, int firstLine, List<String> lines, URL url) throws IOException {
    int count = firstLine;
    Object result = null;

    try {
      while (true) {
        String line = br.readLine();
        if (line == null) {
          break;
        } else {
          lines.add(line);
          count++;
        }
      }
      return result;
    } finally {
      br.close();
    }
  }
  ///////////////////

//  /**
//   * List directory contents for a resource folder. Not recursive.
//   * This is basically a brute-force implementation.
//   * Works for regular files and also JARs.
//   *
//   * @param clazz Any java class that lives in the same place as the resources you want.
//   * @param path  Should end with "/", but not start with one.
//   * @return Just the name of each member item, not the full paths.
//   * @throws URISyntaxException
//   * @throws IOException
//   * @author Greg Briggs
//   */
//  String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
//    URL dirURL = clazz.getClassLoader().getResource(path);
//    if (dirURL != null && dirURL.getProtocol().equals("file")) {
//      /* A file path: easy enough */
//      return new File(dirURL.toURI()).list();
//    }
//
//    if (dirURL == null) {
//      /*
//      * In case of a jar file, we can't actually find a directory.
//      * Have to assume the same jar as clazz.
//      */
//      String me = clazz.getName().replace(".", "/") + ".class";
//      dirURL = clazz.getClassLoader().getResource(me);
//    }
//
//    if (dirURL.getProtocol().equals("jar")) {
//      /* A JAR path */
//      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
//      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
//      Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
//      Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
//      while (entries.hasMoreElements()) {
//        String name = entries.nextElement().getName();
//        if (name.startsWith(path)) { //filter according to the path
//          String entry = name.substring(path.length());
//          int checkSubdir = entry.indexOf("/");
//          if (checkSubdir >= 0) {
//            // if it is a subdirectory, we just return the directory name
//            entry = entry.substring(0, checkSubdir);
//          }
//          result.add(entry);
//        }
//      }
//      return result.toArray(new String[result.size()]);
//    }
//
//    throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
//  }
//
//  ///////////////
//
//  /**
//   * for all elements of java.class.path get a Collection of resources
//   * Pattern pattern = Pattern.compile(".*"); gets all resources
//   *
//   * @param pattern the pattern to match
//   * @return the resources in the order they are found
//   */
//  public static Collection<String> getResources(Pattern pattern) {
//    ArrayList<String> retval = new ArrayList<String>();
//    String classPath = System.getProperty("java.class.path", ".");
//    String[] classPathElements = classPath.split(":");
//    for (String element : classPathElements) {
//      retval.addAll(getResources(element, pattern));
//    }
//    return retval;
//  }
//
//  private static Collection<String> getResources(String element, Pattern pattern) {
//    ArrayList<String> retval = new ArrayList<String>();
//    File file = new File(element);
//    if (file.isDirectory()) {
//      retval.addAll(getResourcesFromDirectory(file, pattern));
//    } else {
//      retval.addAll(getResourcesFromJarFile(file, pattern));
//    }
//    return retval;
//  }
//
//  private static Collection<String> getResourcesFromJarFile(File file, Pattern pattern) {
//    ArrayList<String> retval = new ArrayList<String>();
//    ZipFile zf;
//    try {
//      zf = new ZipFile(file);
//    } catch (ZipException e) {
//      throw new Error(e);
//    } catch (IOException e) {
//      throw new Error(e);
//    }
//    Enumeration e = zf.entries();
//    while (e.hasMoreElements()) {
//      ZipEntry ze = (ZipEntry) e.nextElement();
//      String fileName = ze.getName();
//      boolean accept = pattern.matcher(fileName).matches();
//      if (accept) {
//        retval.add(fileName);
//      }
//    }
//    try {
//      zf.close();
//    } catch (IOException e1) {
//      throw new Error(e1);
//    }
//    return retval;
//  }
//
//  private static Collection<String> getResourcesFromDirectory(File directory, Pattern pattern) {
//    ArrayList<String> retval = new ArrayList<String>();
//    File[] fileList = directory.listFiles();
//    for (File file : fileList) {
//      if (file.isDirectory()) {
//        retval.addAll(getResourcesFromDirectory(file, pattern));
//      } else {
//        try {
//          String fileName = file.getCanonicalPath();
//          boolean accept = pattern.matcher(fileName).matches();
//          if (accept) {
//            retval.add(fileName);
//          }
//        } catch (IOException e) {
//          throw new Error(e);
//        }
//      }
//    }
//    return retval;
//  }

}
