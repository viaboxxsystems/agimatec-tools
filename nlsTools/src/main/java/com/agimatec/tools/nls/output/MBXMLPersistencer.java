package com.agimatec.tools.nls.output;

import com.agimatec.commons.util.FileUtils;
import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:25:25 <br/>
 * Copyright: Agimatec GmbH
 */
public class MBXMLPersistencer extends MBPersistencer {
    static final XStream xstream = new XStream();
    
    static {
        configure(xstream);
    }

    static void configure(XStream xstream) {
        xstream.processAnnotations(MBBundle.class);
        xstream.processAnnotations(MBBundles.class);
        xstream.processAnnotations(MBEntry.class);
//        xstream.processAnnotations(MBText.class);
       xstream.registerConverter(new MBTextConverter());
    }

    public void save(MBBundles obj, File target) throws IOException {
        Writer out = FileUtils.openFileWriterUTF8(target);
        try {
            xstream.toXML(obj, out);
        } finally {
            out.close();
        }
    }

    public MBBundles load(File source) throws IOException, ClassNotFoundException {
        Reader reader = FileUtils.openFileReaderUTF8(source);
        try {
            return (MBBundles) xstream.fromXML(reader);
        } finally {
            reader.close();
        }
    }

    public static XStream getXstream() {
        return xstream;
    }
}
