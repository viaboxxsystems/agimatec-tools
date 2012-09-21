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
 * Copyright: Agimatec GmbH
 */
public class XStreamPersistencer implements ObjectPersistencer {
    static final XStream xstream = new XStream();

    static {
        xstream.processAnnotations(HistSchemaConfig.class);
        xstream.processAnnotations(HistTableConfig.class);
        xstream.processAnnotations(CatalogDescription.class);
        xstream.processAnnotations(ColumnDescription.class);
        xstream.processAnnotations(ForeignKeyDescription.class);
        xstream.processAnnotations(IndexDescription.class);
        xstream.processAnnotations(SequenceDescription.class);
        xstream.processAnnotations(TableDescription.class);
        xstream.processAnnotations(CatalogConversion.class);
        xstream.processAnnotations(DataType.class);
        xstream.processAnnotations(DataTypeTransformation.class);
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
