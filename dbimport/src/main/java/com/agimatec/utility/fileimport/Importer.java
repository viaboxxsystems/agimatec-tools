package com.agimatec.utility.fileimport;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Description: API facade to import a document (file) in any format.<br/>
 * User: roman.stumm <br/>
 * Date: 20.11.2007 <br/>
 * Time: 16:20:21 <br/>
 * Copyright: Agimatec GmbH
 */
public class Importer {
    protected static final Logger log = Logger.getLogger(Importer.class);
    /** processor is responsible to do the actual import process */
    protected final ImporterProcessor processor;
    /** is determines the input format and configuration */
    protected final ImporterSpec spec;

    public Importer(ImporterSpec spec) {
        this.spec = spec;
        this.processor = spec.createProcessor(this);
    }

    /**
     * @return the import specification
     */
    protected ImporterSpec getSpec() {
        return spec;
    }

    /**
     * set the writer where errorous data rows should be stored to.
     * this is to create an error file that has the same format
     * than the import file.
     * @param aErrorWriter
     */
    public final void setErrorWriter(Writer aErrorWriter) {
        processor.setErrorWriter(aErrorWriter);
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
        processor.importFrom(aReader);
    }

    /**
     * API - this method starts the import process.
     * It imports the data read from the given stream.
     * The stream is closed afterwards.
     *
     * @param aStream - stream to read the import data from.
     * @throws Exception
     */
    public void importFrom(InputStream aStream) throws ImporterException {
        processor.importFrom(aStream);
    }

    /**
     * @return number of rows (data entries) successfully processed
     */
    public int getRowCount() {
        return processor.getRowCount();
    }

    /**
     * @return number of errors found during import (data entries not imported)
     */
    public int getErrorCount() {
        return processor.getErrorCount();
    }

    /**
     * @return true when import has been cancelled
     */
    public boolean isCancelled() {
        return processor.isCancelled();
    }

    /**
     * last exception thrown and handled during import processing
     * @return null or a throwable
     */
    public Throwable getLastException() {
        Object err = processor.getLastError();
        return (err instanceof Throwable) ? (Throwable) err : null;
    }

    /**
     * last error message that occured during import processing
     * @return null or the message string
     */
    public String getLastErrorMessage() {
        Object err = processor.getLastError();
        if (err instanceof Throwable) {
            return ((Throwable) err).getLocalizedMessage();
        } else if (err != null) {
            return String.valueOf(err);
        } else {
            return null;
        }
    }

    /** log an info message to the importLog. */
    public void log(Object obj) {
        if (obj instanceof Throwable) {
            log.error(null, (Throwable) obj);
        } else {
            log.info(obj);
        }
    }

    public ImporterProcessor getProcessor() {
        return processor;
    }
}
