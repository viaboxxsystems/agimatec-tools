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
    /**
     * processor is responsible to do the actual import process
     */
    protected final ImporterProcessor processor;
    /**
     * is determines the input format and configuration
     */
    protected final ImporterSpec spec;

    public Importer(ImporterSpec spec) {
        this.spec = spec;
        this.processor = spec.createProcessor(this);
    }

    protected ImporterSpec getSpec()
    {
        return spec;
    }

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

    public int getRowCount() {
        return processor.getRowCount();
    }

    public int getErrorCount() {
        return processor.getErrorCount();
    }

    public boolean isCancelled() {
        return processor.isCancelled();
    }

    /** log an info message to the importLog. */
    public void log(Object obj) {
        if (obj instanceof Throwable) {
            log.error(null, (Throwable) obj);
        } else {
            log.info(obj);
        }
    }

}
