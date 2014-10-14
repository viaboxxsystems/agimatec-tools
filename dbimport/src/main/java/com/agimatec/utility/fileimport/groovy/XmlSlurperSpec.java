package com.agimatec.utility.fileimport.groovy;

import com.agimatec.utility.fileimport.ImporterProcessor;
import com.agimatec.utility.fileimport.ImporterSpec;
import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.Iterator;

/**
 * Description: Specify how to use groovy XmlSlurper with provided Closures to
 * handle the import<br/>
 * User: roman.stumm <br/>
 * Date: 20.11.2007 <br/>
 * Time: 15:24:53 <br/>
 */
public class XmlSlurperSpec implements ImporterSpec {
    protected static final Logger log = LoggerFactory.getLogger(XmlSlurperSpec.class);
    private final Closure headerProcessing;
    private final Closure gpathRetriever;
    private final Closure elementProcessing;

    public XmlSlurperSpec(Closure nextProcessing, Closure elementProcessing) {
        this(null, nextProcessing, elementProcessing);
    }
    
    public XmlSlurperSpec(Closure headerProcessing, Closure gpathRetriever,
                          Closure elementProcessing) {
        this.headerProcessing = headerProcessing;
        this.gpathRetriever = gpathRetriever;
        this.elementProcessing = elementProcessing;
    }

    public Writer getErrorWriter() {
        return null;  
    }

    public ImporterProcessor createProcessor(com.agimatec.utility.fileimport.Importer importer) {
        return new XmlImportProcessor(this, importer);
    }

    public void processFirst(XmlImportProcessor processor) {
        if (headerProcessing != null) headerProcessing.call(processor.getCurrent());
    }

    public Iterator iterator(XmlImportProcessor processor) {
        return ((GPathResult) gpathRetriever.call(processor.getCurrent()))
                .iterator();
    }

    public void processEach(XmlImportProcessor processor) {
        if (elementProcessing == null) {
            log.info("element " + processor.getRowCount() + ": " +
                    processor.getCurrent());
        } else {
            elementProcessing.call(processor);
        }
    }
}
