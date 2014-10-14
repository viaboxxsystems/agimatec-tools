package com.agimatec.utility.fileimport.groovy;

import com.agimatec.utility.fileimport.ImporterException;
import com.agimatec.utility.fileimport.LineImportProcessor;
import com.agimatec.utility.fileimport.LineImporterSpecAutoFields;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerInvocationException;

/**
 * Description: This subclass makes it easy in a groovy script to
 * customize the processing-behavior of the importer <br/>
 * example 1:<pre>
 def spec = new LineImporterSpecGroovy({ processor ->
    println "inside header closure " + processor.headerLine;
 },
 { processor ->
        println "inside row closure " + processor.rowCount + ": " + processor.currentRow;
 });
 * </pre>
 * example 2:<pre>
 def spec = new LineImporterSpecGroovy({ processor ->
        println "inside row closure " + processor.rowCount + ": " + processor.currentRow;
 });
  </pre>
 * User: roman.stumm <br/>
 * Date: 30.08.2007 <br/>
 * Time: 10:57:42 <br/>
 */
public class LineImporterSpecGroovy extends LineImporterSpecAutoFields {
    private final Closure rowProcessing;
    private final Closure headerProcessing;

    public LineImporterSpecGroovy(Closure rowProcessing) {
        this.headerProcessing = null;
        this.rowProcessing = rowProcessing;
    }

    public LineImporterSpecGroovy(Closure headerProcessing, Closure rowProcessing) {
        this.headerProcessing = headerProcessing;
        this.rowProcessing = rowProcessing;
    }

    @Override
    public void processHeaderLine(LineImportProcessor processorimporter) throws ImporterException {
        if (headerProcessing == null) {
            super.processHeaderLine(processorimporter);   // call super!
        } else {
            headerProcessing.call(processorimporter);
        }
    }

    @Override
    public void processRow(LineImportProcessor processor) throws ImporterException {
        try {
            rowProcessing.call(processor);
        } catch(InvokerInvocationException ex) {
            if(ex.getCause() instanceof ImporterException) {
                throw (ImporterException)ex.getCause();
            } else {
                throw ex;
            }
        }
    }
}
