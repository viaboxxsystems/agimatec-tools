package com.agimatec.commons.util;

import java.io.*;
import java.nio.charset.Charset;

/**
 * <p>Description: Provides methods to read files.</p>
 * <p>Copyright (c) 2007</p>
 * <p>Company: Agimatec GmbH </p>
 *
 * @author Roman Stumm
 */
public final class FileUtils {

    public static Writer openFileWriterUTF8(File file) throws FileNotFoundException {
        return openFileWriter(file, "UTF-8");
    }

    public static Writer openFileWriter(File file, String encoding)
            throws FileNotFoundException {
        return new OutputStreamWriter(new FileOutputStream(file),
                Charset.forName(encoding));
    }

    public static Reader openFileReaderUTF8(File file) throws FileNotFoundException {
        return openFileReader(file, "UTF-8");
    }

    public static Reader openFileReader(File file, String encoding)
            throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(file),
                Charset.forName(encoding));
    }
}
