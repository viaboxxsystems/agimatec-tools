package com.agimatec.utility.fileimport;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 16:51:07 <br/>
 */
public class ImporterException extends RuntimeException {
    /** set to true when import should immediately stop */
    private final boolean cancelImport;

    public ImporterException(Throwable cause, boolean stop) {
        super(cause);
        cancelImport = stop;
    }

    public ImporterException(String message, boolean stop) {
        super(message);
        cancelImport = stop;
    }

    public ImporterException(String message, Throwable cause, boolean stop) {
        super(message, cause);
        cancelImport = stop;
    }

    public boolean isCancelImport() {
        return cancelImport;
    }
}
