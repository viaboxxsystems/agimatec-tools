package com.agimatec.database;

import org.dbunit.dataset.DataSetException;

import java.io.IOException;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 12:27:10 <br/>
 * Copyright: Agimatec GmbH
 */
public class DataSetFactory {
    /**
     * .xml --> FlatXmlDataSet<br>
     * .xxml --> XmlDataSet<br>
     * .xls --> XslDataSet<br>
     * <br>
     *
     * @param dataFile
     * @return adequate dataset
     * @throws IOException
     * @throws DataSetException
     */
    public static DataSet createDataSet(String dataFile) {
        if (dataFile.endsWith(".xml")) {
            return new DataSetFlatXML(dataFile);
        } else if (dataFile.endsWith(".xxml")) {
            return new DataSetXML(dataFile);
        } else if (dataFile.endsWith(".xls")) {
            return new DataSetXSL(dataFile);
        } else {
            throw new IllegalArgumentException("Unsupport file format for " + dataFile);
        }
    }
}
