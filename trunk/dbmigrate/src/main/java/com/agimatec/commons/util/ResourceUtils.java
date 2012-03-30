package com.agimatec.commons.util;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.03.2010<br>
 * Time: 11:23:03<br>
 * viaboxx GmbH, 2010
 */
public class ResourceUtils {
    /**
     * Reads the URL contents into a list, with one element for each line.
     *
     * @param url a URL
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @since 1.6.8
     */
    public static List<String> readLines(URL url) throws IOException {
        return readLines(newReader(url));
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
    public static List<String> readLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList();
        eachLine(reader, lines);
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
    private static BufferedReader newReader(URL url) throws IOException {
        return newReader(url.openConnection().getInputStream());
    }

    /**
     * Creates a reader for this input stream.
     *
     * @param stream an input stream
     * @return a reader
     * @since 1.0
     */
    private static BufferedReader newReader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Iterates through the given reader line by line.
     *
     * @param br a Reader, closed after the method returns
     * @throws IOException if an IOException occurs.
     * @since 1.5.7
     */
    private static void eachLine(BufferedReader br, List<String> lines) throws IOException {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                } else {
                    lines.add(line);
                }
            }
        } finally {
            br.close();
        }
    }

    public static Collection<String> getResources(String resourceDir) throws IOException {
        Enumeration<URL> enums = Thread.currentThread().getContextClassLoader().getResources(resourceDir);
        Set<String> all = new HashSet<String>();
        while (enums.hasMoreElements()) {
            URL url = enums.nextElement();
            all.addAll(getURLResources(url));
        }
        return all;
    }

    /**
     * Reads the URL contents into a list, can read jars (jar:file:path!package) and directories (file:path/package)
     *
     * @param dirURL a URL (a jar or file)
     * @return a List or Set of lines
     * @throws IOException if an IOException occurs.
     */
    public static Collection<String> getURLResources(URL dirURL) throws IOException {
        if (dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough, but need to decode when path contains "blanks" (tested under windows) */
            try {
                return Arrays.asList(new File(URLDecoder.decode(dirURL.getPath(), "UTF-8")).list());
            } catch (Exception ignore) {
                try {
                    return Arrays.asList(new File(dirURL.getPath()).list());
                } catch (Exception ex) {
                    throw new IOException("Cannot list directory " + dirURL.getPath(), ex);
                }
            }
        } else if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String urlPath = dirURL.getPath();
            int idx2 = urlPath.indexOf("!");
            String jarPath = urlPath.substring(5, idx2); //strip out only the JAR file
            String resourceDir = urlPath.substring(idx2 + 2, urlPath.length());
            if (!resourceDir.endsWith("/")) resourceDir = resourceDir + "/";
            JarFile jar;
            try {
                // there that say so: http://stackoverflow.com/questions/6247144/how-to-load-a-folder-from-a-jar
                jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            } catch (Exception ignore) {
                try {
                    // but on my Mac this sometimes doesn't work, but this works:
                    jar = new JarFile(jarPath);
                } catch (Exception ex) {
                    throw new IOException("Cannot open jar " + jarPath + " in " + dirURL, ex);
                }
            }
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.length() > resourceDir.length() &&
                        name.startsWith(resourceDir)) { //filter according to the path
                    result.add(new File(name).getName());
                }
            }
            return result;
        } else {
            return readLines(dirURL);
        }
    }


}
