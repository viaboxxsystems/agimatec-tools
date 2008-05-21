package com.agimatec.utility.fileimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: Import a file line-by-line, e.g. for csv-/comma-separated/fixed-column-length
 * files. <br/>
 * User: roman.stumm <br/>
 * Date: 22.11.2007 <br/>
 * Time: 11:56:57 <br/>
 * Copyright: Agimatec GmbH
 */
public class LineImportProcessor extends ImporterProcessor {
    protected final LineImporterSpec spec;
    protected BufferedReader lineReader;
    protected String headerLine;
    protected Map currentRow;

    public LineImportProcessor(LineImporterSpec spec, Importer importer) {
        super(importer);
        this.spec = spec;
    }

    public LineImporterSpec getSpec() {
        return spec;
    }

    /**
     * API - this method starts the import process.
     * It imports the data read from the given reader.
     * The reader is closed afterwards.
     *
     * @param aReader - reader to read the import data from.
     * @throws Exception
     */
    public void importFrom(Reader aReader) throws ImporterException {
        super.importFrom(aReader);
        lineReader = new BufferedReader(aReader);
        try {
            try {
                if (spec.getHeaderSpec() == LineImporterSpec.Header.FIRST) {
                    // skip header line
                    headerLine = lineReader.readLine();
                    spec.processHeaderLine(this);
                }
                String line = lineReader.readLine();
                while (line != null && !isCancelled()) {
                    rowCount++;
                    importRow(line);
                    line = lineReader.readLine();
                }
            } finally {
                lineReader.close();
                release();
                logFinished();
            }
        } catch (Exception e) {
             handleException(e);
        }
    }

    /**
     * import a record from the file and do what is required with the data.
     *
     * @param aLine - a line of data of one record
     */
    protected void importRow(String aLine) throws ImporterException {
        try {
            transferRow(aLine);
            spec.processRow(this);
        } catch (ImporterException ex) {
            handleRowException(ex, aLine);
            if (ex.isCancelImport()) throw ex;
        } catch (Exception ex) {
            handleRowException(ex, aLine);
        }
    }

    public void release() {
        super.release();
        currentRow = null;
        headerLine = null;
    }

    /** parse a specified record and save the record data in the root model. */
    protected Map transferRow(String aRecord) throws IOException {
        currentRow = new HashMap();
        LineTokenizer parser = spec.getLineTokenizerFactory().createTokenizer(aRecord);
        String singleValue;
        int fieldIdx = 0;
        while (parser.hasMoreElements()) {
            singleValue = parser.nextElement();
            if (parser.isLineIncomplete()) {
                do {
                    aRecord = lineReader.readLine();
                    if (aRecord != null) {
                        singleValue = parser.continueParse(singleValue, aRecord);
                    }
                } while (parser.isLineIncomplete() && aRecord != null);
            }
            setFieldValue(fieldIdx++, singleValue);
        }
        return currentRow;
    }

    protected void setFieldValue(int fieldIdx, String singleValue) {
        currentRow.put(spec.getFieldName(fieldIdx), singleValue);
    }

    protected String getHeaderLine() {
        return headerLine;
    }

    public Map getCurrentRow() {
        return currentRow;
    }

    public BufferedReader getLineReader() {
        return lineReader;
    }
}
