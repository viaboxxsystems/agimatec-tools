package com.agimatec.sql.meta.persistence;

import com.agimatec.dbhistory.HistSchemaConfig;
import com.agimatec.dbhistory.HistTableConfig;
import com.agimatec.dbtransform.CatalogConversion;
import com.agimatec.dbtransform.DataType;
import com.agimatec.dbtransform.DataTypeTransformation;
import com.agimatec.sql.meta.*;
import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 17:39:36 <br/>
 */
public class XStreamPersistencer implements ObjectPersistencer {
    static final XStream xstream = new XStream();

    static {
        Class[] types =
            {HistSchemaConfig.class, HistTableConfig.class,
                CatalogDescription.class, ColumnDescription.class,
                ForeignKeyDescription.class, IndexDescription.class,
                SequenceDescription.class, TableDescription.class,
                CatalogConversion.class,
                DataType.class, DataTypeTransformation.class};
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypes(types);
        xstream.processAnnotations(types);
    }

    static Charset charset = Charset.forName("UTF-8");

    public void save(Object obj, File target) throws IOException {
        OutputStreamWriter out =
            new OutputStreamWriter(new FileOutputStream(target), charset);
        try {
            xstream.toXML(obj, out);
        } finally {
            out.close();
        }
    }

    public Object load(File source) throws IOException, ClassNotFoundException {
        InputStreamReader reader =
            new InputStreamReader(new FileInputStream(source), charset);
        try {
            return xstream.fromXML(reader);
        } finally {
            reader.close();
        }
    }
}
