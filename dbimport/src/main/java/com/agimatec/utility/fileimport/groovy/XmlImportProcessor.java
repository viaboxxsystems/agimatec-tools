package com.agimatec.utility.fileimport.groovy;

import com.agimatec.utility.fileimport.Importer;
import com.agimatec.utility.fileimport.ImporterException;
import com.agimatec.utility.fileimport.ImporterProcessor;
import groovy.util.XmlSlurper;

import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

/**
 * Description: Process a xml document with the groovy XmlSlurper.<br/>
 * User: roman.stumm <br/>
 * Date: 22.11.2007 <br/>
 * Time: 11:56:33 <br/>
 */
public class XmlImportProcessor extends ImporterProcessor {
    protected final XmlSlurperSpec spec;
    protected XmlSlurper xmlSlurper;
    /** actually the current GPathResult, but could be any object * */
    protected Object current;

    public XmlImportProcessor(XmlSlurperSpec spec, Importer importer) {
        super(importer);
        this.spec = spec;
    }

    public XmlSlurperSpec getSpec() {
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
    @Override
    public void importFrom(Closeable aReader) throws ImporterException {
        super.importFrom(aReader);
        try {
            xmlSlurper = new XmlSlurper();
            try {
                if (aReader instanceof InputStream) {
                    current = xmlSlurper.parse((InputStream) aReader);
                } else {
                    current = xmlSlurper.parse((Reader) aReader);
                }
                spec.processFirst(this);
                Iterator iter = spec.iterator(this);
                while (iter.hasNext() && !isCancelled()) {
                    rowCount++;
                    current = iter.next();
                    importEach();
                }
            } finally {
                aReader.close();
                release();
                logFinished();
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    protected void importEach() throws ImporterException {
        try {
            spec.processEach(this);
        } catch (ImporterException ex) {
            handleRowException(ex, current);
            if (ex.isCancelImport()) throw ex;
        } catch (Exception ex) {
            handleRowException(ex, current);
        }
    }

    public void release() {
        super.release();
        current = null;
    }

    public Object getCurrent() {
        return current;
    }

    public XmlSlurper getXmlSlurper() {
        return xmlSlurper;
    }
}
