package com.agimatec.utility.fileimport;

import com.agimatec.utility.fileimport.spreadsheet.ICell;

import java.io.Closeable;
import java.io.IOException;
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
    protected LineReader lineReader;
    protected Object headerLine;
    protected Object currentLine;
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
    public void importFrom(Closeable aReader) throws ImporterException {
        super.importFrom(aReader);
        try {
            lineReader = spec.createLineReader(aReader);
            try {
                if (spec.getHeaderSpec() == LineImporterSpec.Header.FIRST) {
                    // skip header line
                    headerLine = lineReader.readLine();
                    spec.processHeaderLine(this);
                }
                currentLine = lineReader.readLine();
                while (currentLine != null && !isCancelled()) {
                    rowCount++;
                    if (spec.getHeaderSpec() == LineImporterSpec.Header.INDEX &&
                            spec.getHeaderLineIndex() == rowCount) {
                        headerLine = currentLine;
                        spec.processHeaderLine(this);
                    } else {
                        importRow(currentLine);
                    }
                    currentLine = lineReader.readLine();
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
    protected void importRow(Object aLine) throws ImporterException {
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
    protected Map transferRow(Object aRecord) throws IOException {
        currentRow = new HashMap();
        LineTokenizer parser = spec.getLineTokenizerFactory().createTokenizer(aRecord);
        Object singleValue;
        int fieldIdx = -1;
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
            if(singleValue instanceof ICell) {
                fieldIdx = ((ICell)singleValue).getColumnIndex();
            } else {
                fieldIdx++;
            }
            setFieldValue(fieldIdx, singleValue);
        }
        return currentRow;
    }

    protected void setFieldValue(int fieldIdx, Object singleValue) {
        currentRow.put(spec.getFieldName(fieldIdx), singleValue);
    }

    protected Object getHeaderLine() {
        return headerLine;
    }

    /**
     * the current row/line itself as it was transferred to a map 
     * @return
     */
    public Map getCurrentRow() {
        return currentRow;
    }

    public LineReader getLineReader() {
        return lineReader;
    }

    /**
     * the current row/line itself as it came from the LineReader
     * @return
     */
    public Object getCurrentLine() {
        return currentLine;
    }
}
