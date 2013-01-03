package com.agimatec.utility.fileimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Description:  Not thread-safe! Specify the behavior of an Importer<br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:42:15 <br/>
 * Copyright: Agimatec GmbH
 */
public class LineImporterSpec implements ImporterSpec {
    protected static final Logger log = LoggerFactory.getLogger(LineImporterSpec.class);
    private Header headerSpec = Header.NONE;
    private LineTokenizerFactory lineTokenizerFactory;
    private String[] fieldNames;
    private Writer errorWriter;

    /** 1 is default */
    private int headerLineIndex = 1;

    public LineTokenizerFactory getLineTokenizerFactory() {
        return lineTokenizerFactory;
    }

    public void setLineTokenizerFactory(LineTokenizerFactory lineTokenizerFactory) {
        this.lineTokenizerFactory = lineTokenizerFactory;
    }

    public Header getHeaderSpec() {
        return headerSpec;
    }

    public void setHeaderSpec(Header headerSpec) {
        this.headerSpec = headerSpec;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    /** @param fieldNames - array with the fieldNames or null */
    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    public void setErrorWriter(Writer errorWriter) {
        this.errorWriter = errorWriter;
    }

    /** @return null or the writer to write errors to */
    public Writer getErrorWriter() {
        return errorWriter;
    }

    public ImporterProcessor createProcessor(Importer importer) {
        return new LineImportProcessor(this, importer);
    }

    public String getFieldName(int fieldIdx) {
        return (fieldNames == null || fieldNames.length <= fieldIdx) ? "field_" + fieldIdx :
                fieldNames[fieldIdx];
    }

    /**
     * overwrite this method to get the behavior after a row has been completly
     * transfered.
     */
    public void processRow(LineImportProcessor processor) throws ImporterException {
        log.info("row: " + processor.getRowCount() + " = " + processor.getCurrentRow());
    }

    /** overwrite this method to get the behavior after the header line has been read */
    public void processHeaderLine(LineImportProcessor processor) throws ImporterException {
        log.info("header: " + processor.getRowCount() + " = " + processor.getHeaderLine());
    }

    public void setHeaderLineIndex(int index) {
        headerLineIndex = index;
    }

    public int getHeaderLineIndex() {
        return headerLineIndex;
    }

    /**
     * initialize the lineReader. overwrite in subclasses if required
     *
     * @throws Exception
     */
    public LineReader createLineReader(Closeable aReader) throws IOException {
        LineReader lineReader = getLineTokenizerFactory().createLineReader();
        if (aReader instanceof Reader) {
            lineReader.setReader((Reader) aReader);
        } else if (aReader instanceof InputStream) {
            lineReader.setStream((InputStream) aReader);
        }
        return lineReader;
    }

    public static enum Header {
        /** no header line */
        NONE,
        /** first line is the header line, same as INDEX, getHeaderLineIndex()==1 */
        FIRST,
        /**
         * the 1-based index of the header line is given
         * with {@link LineImporterSpec#setHeaderLineIndex(int)}.
         * all lines before the index are are processed with generic field names (field_0, field_1, ...)
         */
        INDEX
    }
}
