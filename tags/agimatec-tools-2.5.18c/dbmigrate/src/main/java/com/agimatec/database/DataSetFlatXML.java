package com.agimatec.database;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 14:15:29 <br/>
 * Copyright: Agimatec GmbH
 */
public class DataSetFlatXML extends DataSet {
    protected DataSetFlatXML(String dataFile) {
        super(dataFile);
    }

    public IDataSet load(InputStream in) throws DataSetException, IOException {
        FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
        return builder.build(in);
    }

    public void write(IDataSet set, OutputStream out)
            throws DataSetException, IOException {
        FlatXmlDataSet.write(set, out);
    }
}
