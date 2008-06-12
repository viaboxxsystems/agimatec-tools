package com.agimatec.utility.fileimport;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: subclass that gets the field names from the first header line
 * and contains some common preconfiguration<br/>
 * User: roman.stumm <br/>
 * Date: 28.08.2007 <br/>
 * Time: 17:28:52 <br/>
 * Copyright: Agimatec GmbH
 */
public class LineImporterSpecAutoFields extends LineImporterSpec {
    /**
     * this implementation assumes, that the header line
     * is the first row in the file.
     */
    public LineImporterSpecAutoFields() {
        setHeaderSpec(LineImporterSpec.Header.FIRST);
        setLineTokenizerFactory(new CSVStringTokenizerFactory());
    }

    /**
     * this implementation assumes, that the first line
     * contains the header line with field names
     * separated by the same separator as the data lines.
     */
    @Override
    public void processHeaderLine(LineImportProcessor processor) throws ImporterException {
        LineTokenizer tokens = getLineTokenizerFactory().createTokenizer(processor.getHeaderLine());
        List<String> fieldNames = new ArrayList();
        while (tokens.hasMoreElements()) {
            final Object val = tokens.nextElement();
            fieldNames.add(val == null ? null : String.valueOf(val));
        }
        setFieldNames(fieldNames.toArray(new String[fieldNames.size()]));
    }
}
