package com.agimatec.database;

import com.agimatec.commons.config.ConfigManager;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 14:12:17 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class DataSet {
    protected String dataFile;

    public abstract IDataSet load(InputStream in) throws DataSetException, IOException;

    public abstract void write(IDataSet set, OutputStream out)
            throws DataSetException, IOException;

    protected DataSet(String dataFile) {
        this.dataFile = dataFile;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * load from file ("./data.xml") or from classpath ("data.xml")
     *
     * @return
     * @throws IOException
     * @throws DataSetException
     */
    public IDataSet load() throws IOException, DataSetException {
        IDataSet testDataSet;
        InputStream in = ConfigManager.toURL(dataFile).openStream();
        try {
            testDataSet = load(in);
        } finally {
            in.close();
        }
        return testDataSet;
    }
}
