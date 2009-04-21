package com.agimatec.utility.fileimport;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Description: common implementation to do the import process<br/>
 * User: roman.stumm <br/>
 * Date: 22.11.2007 <br/>
 * Time: 11:56:47 <br/>
 * Copyright: Agimatec GmbH
 */
public abstract class ImporterProcessor {
    protected final Importer importer;
    private Writer errorWriter;
    protected int errorCount;
    protected int rowCount;
    protected boolean cancelled = false;
    protected Object lastError;

    protected abstract ImporterSpec getSpec();

    protected ImporterProcessor(Importer importer) {
        this.importer = importer;
    }

    public Importer getImporter() {
        return importer;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public Object getLastError() {
        return lastError;
    }

    public void setLastError(Object lastError) {
        this.lastError = lastError;
    }

    public Writer getErrorWriter() throws IOException {
        if (errorWriter == null) {
            Writer theWriter = getSpec().getErrorWriter();
            if (theWriter == null) {
                errorWriter = new OutputStreamWriter(System.err);
            } else return theWriter;
        }
        return errorWriter;
    }

    public void setErrorWriter(Writer aErrorWriter) {
        errorWriter = aErrorWriter;
    }

    public void log(Object obj)
    {
        importer.log(obj);
    }

    protected void importFrom(Closeable aReader) {
        errorCount = 0;
        rowCount = 0;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    protected void logFinished() {
        importer.log("Done. Rows/errors = " + rowCount + "/" + errorCount);
    }

    public void release() {
        try {
            if (errorWriter != null) errorWriter.close();
        } catch (Exception ex) {
            importer.log(ex);
        }
        errorWriter = null;
    }

    protected void handleException(Exception ex) throws ImporterException {
        if(ex != null) {
            setLastError(ex);
        }
        if (ex instanceof ImporterException) {
            if (((ImporterException) ex).isCancelImport()) cancelled = true;
            throw (ImporterException) ex;
        } else {
            cancelled = true;
            throw new ImporterException(ex, true);
        }
    }

    /** overwrite in subclasses for different exception handling */
    protected void handleRowException(Exception iex, Object aLine) {
        if (iex instanceof ImporterException) {
            if (((ImporterException) iex).isCancelImport()) cancelled = true;
        }
        errorCount++;
        setLastError(iex);
        importer.log("'" + aLine + "' caused exception:");
        importer.log(iex);
        try {
            getErrorWriter().write(String.valueOf(aLine));
            getErrorWriter().write('\n');
        } catch (Exception ex) {
            importer.log(ex);
        }
    }
}
