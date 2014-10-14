package com.agimatec.database;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 14:15:36 <br/>
 */
public class DataSetXML extends DataSet {
    protected DataSetXML(String dataFile) {
        super(dataFile);
    }

    public IDataSet load(InputStream in) throws DataSetException, IOException {
        return new XmlDataSet(in);
    }

    public void write(IDataSet set, OutputStream out)
            throws DataSetException, IOException {
        XmlDataSet.write(set, out);
    }
}