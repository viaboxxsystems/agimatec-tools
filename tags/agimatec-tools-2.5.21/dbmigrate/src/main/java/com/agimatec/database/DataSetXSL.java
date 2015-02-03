package com.agimatec.database;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 14:15:42 <br/>
 */
public class DataSetXSL extends DataSet {
    protected DataSetXSL(String dataFile) {
        super(dataFile);
    }

    public IDataSet load(InputStream in) throws DataSetException, IOException {
        return new XlsDataSet(in);
    }

    public void write(IDataSet set, OutputStream out)
            throws DataSetException, IOException {
        XlsDataSet.write(set, out);
    }
}
