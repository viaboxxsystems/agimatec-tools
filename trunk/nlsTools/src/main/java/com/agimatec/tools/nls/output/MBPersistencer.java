package com.agimatec.tools.nls.output;

import com.agimatec.tools.nls.model.MBBundles;

import java.io.File;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.12.2010<br>
 * Time: 16:41:46<br>
 * viaboxx GmbH, 2010
 */
public abstract class MBPersistencer {
    public abstract void save(MBBundles obj, File target) throws Exception;

    public abstract MBBundles load(File source) throws Exception;

    public static MBPersistencer forFile(String aFile) {
        return forFile(new File(aFile));
    }

    public static MBPersistencer forFile(File aFile) {
        String name = aFile.getName().toLowerCase();
        if (name.endsWith(".xls")) {
            return new MBExcelPersistencer();
        } else if (name.endsWith(".xml")) {
            return new MBXMLPersistencer();
        } else if (name.endsWith(".js")) {
            return new MBJSONPersistencer(true);
        } else {
            throw new IllegalArgumentException("File type not supported: " + aFile);
        }
    }

    public static MBBundles loadFile(File aFile) throws Exception {
        return forFile(aFile).load(aFile);
    }

    public static void saveFile(MBBundles obj, File aFile) throws Exception {
        forFile(aFile).save(obj, aFile);
    }
}
