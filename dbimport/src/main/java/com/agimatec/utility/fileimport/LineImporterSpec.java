package com.agimatec.utility.fileimport;

import java.io.Writer;

/**
 * Description:  Not thread-safe! Specify the behavior of an Importer<br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:42:15 <br/>
 * Copyright: Agimatec GmbH
 */
public class LineImporterSpec implements ImporterSpec {
    private Header headerSpec = Header.NONE;
    private LineTokenizerFactory lineTokenizerFactory;
    private String[] fieldNames;
    private Writer errorWriter;

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
        return (fieldNames == null || fieldNames.length <= fieldIdx) ?
                "field_" + fieldIdx : fieldNames[fieldIdx];
    }

    /**
     * overwrite this method to get the behavior after a row has been completly
     * transfered.
     */
    public void processRow(LineImportProcessor processor) throws ImporterException {
        System.out.println("row: " + processor.getRowCount() + " = " +
                processor.getCurrentRow());
    }

    /** overwrite this method to get the behavior after the header line has been read */
    public void processHeaderLine(LineImportProcessor processor) throws ImporterException {
        System.out.println("header: " + processor.getHeaderLine());
    }

    public static enum Header {
        /** no header line */
        NONE,
        /** first line is the header line */
        FIRST
    }
}
