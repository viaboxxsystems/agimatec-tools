package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBFile;
import com.agimatec.tools.nls.output.MBBundlesZipper;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: copy files mentioned in 'masterFile' to the directory 'dest'
 * or a zip file 'dest'.
 * zip = true: zip (this is the default behavior)
 * masterFile = file with file names to copy.
 * dest = target directory or target zip-file to write.
 * <br/>
 * example:
 * <pre>
 * &lt;taskdef name="copybundles"
 *      classname="com.agimatec.tools.nls.CopyBundlesTask"&gt;
 *   &lt;classpath refid="maven.test.classpath"/&gt;
 * &lt;/taskdef&gt;
 * &lt;copybundles masterFile="allBundles.txt"
 *          dest="target/bundles.zip"/&gt;
 *
 * ------------------------------
 * file allBundles.txt (example):
 * common.xml
 * customer.xml
 * orders.xml
 * ------------------------------
 * </pre>
 * <br/>
 * User: roman <br/>
 * Date: 09.02.2009 <br/>
 * Time: 16:05:43 <br/>
 * Copyright: Agimatec GmbH
 */
public class CopyBundlesTask extends Task {
    private File masterFile;
    /** target directory (zip==false) or target file (zip==true) */
    private File dest;
    private boolean zip = true;

    public File getMasterFile() {
        return masterFile;
    }

    public void setMasterFile(File masterFile) {
        this.masterFile = masterFile;
    }

    public File getDest() {
        return dest;
    }

    public void setDest(File dest) {
        this.dest = dest;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(boolean zip) {
        this.zip = zip;
    }

    public void execute() {
        if (masterFile == null) throw new BuildException("masterFile required");
        if (dest == null) dest = new File(getProject().getBaseDir(), "bundles");
        try {
            Map<String, File> mappings = readControlFileMapping(masterFile);
            if (!zip) {
                dest.mkdirs();
                for (Map.Entry<String, File> entry : mappings.entrySet()) {
                    File source = entry.getValue();

                    File target = new File(dest, entry.getKey());
                    FileUtils.getFileUtils().copyFile(source, target);
                }
            } else {
                if (dest.getParentFile() != null) {
                    dest.getParentFile().mkdirs();
                }
                List<MBFile> files = new ArrayList();
                for (Map.Entry<String, File> entry : mappings.entrySet()) {
                    File source = entry.getValue();
                    MBFile file = new MBFile();
                    FileReader reader = new FileReader(source);
                    file.setContent(IOUtils.toString(reader));
                    file.setLastModified(source.lastModified());
                    file.setName(entry.getKey());
                    reader.close();
                    files.add(file);
                }
                FileOutputStream fout = new FileOutputStream(dest);
                MBBundlesZipper.zip(files, fout);
                fout.close();
            }
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    /**
     * @param controlFile
     * @return  key = plain name.xml, value = xml-bundle file
     * @throws IOException
     */
    static Map<String, File> readControlFileMapping(File controlFile)
          throws IOException {
        FileReader reader = new FileReader(controlFile);
        List<String> controlFileContent = IOUtils.readLines(reader);
        reader.close();

        Map<String, File> controlFileMapping = new HashMap(controlFileContent.size());
        for (String line : controlFileContent) {
            line = line.trim();
            if (line.length() > 0) {
                int i = line.lastIndexOf('/');
                String name = line.substring(i + 1);
                controlFileMapping.put(name, new File(controlFile.getParent(), line));
            }
        }
        return controlFileMapping;
    }
}
