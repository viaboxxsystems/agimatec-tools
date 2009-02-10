package com.agimatec.tools.nls.output;

import com.agimatec.tools.nls.model.MBFile;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Description: <br/>
 * User: roman <br/>
 * Date: 10.02.2009 <br/>
 * Time: 14:18:13 <br/>
 * Copyright: Agimatec GmbH
 */
public class MBBundlesZipper {

    public static void zip(List<MBFile> files, OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        zipOut.setComment(files.size() + " nls files (copied: " +
              new Timestamp(System.currentTimeMillis()) + ")");
        for (MBFile file : files) {
            String fname;
            if (file.getName().endsWith(".xml")) {
                fname = file.getName();
            } else {
                fname = file.getName() + ".xml";
            }
            ZipEntry entry = new ZipEntry(fname);
            byte[] bytes = file.getContent().getBytes();
            entry.setTime(file.getLastModified());
            entry.setSize(bytes.length);
            zipOut.putNextEntry(entry);
            zipOut.write(bytes);
            zipOut.closeEntry();
        }
        zipOut.close();
    }

    public static List<MBFile> unzipFiles(InputStream fin) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(fin);
        ZipEntry entry;
        List<MBFile> files = new ArrayList();
        while ((entry = zipIn.getNextEntry()) != null) {
            byte[] buf = new byte[2048];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int size;
            while ((size = zipIn.read(buf)) != -1) {
                baos.write(buf, 0, size);
            }
            baos.close();
            String fileName = entry.getName();
            if (!fileName.endsWith(".xml")) {
                fileName = fileName + ".xml";
            }
            MBFile file = new MBFile();
            file.setName(fileName);
            file.setContent(baos.toString());
            file.setLastModified(entry.getTime());
            files.add(file);
            zipIn.closeEntry();
        }
        return files;
    }

    public static List<MBFile> unzipFiles(byte[] zipped) throws IOException {
        return unzipFiles(new ByteArrayInputStream(zipped));
    }
}
