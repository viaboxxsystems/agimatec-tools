package com.agimatec.utility.fileimport;

import com.agimatec.utility.fileimport.spreadsheet.ICell;

import java.util.HashMap;
import java.util.Map;

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
    public void processHeaderLine(LineImportProcessor processor)
          throws ImporterException {
        LineTokenizer tokens =
              getLineTokenizerFactory().createTokenizer(processor.getHeaderLine());
        Map<Integer, String> fieldNames = new HashMap();
        int fieldIdx = -1;
        int maxFieldIdx = 0;
        while (tokens.hasMoreElements()) {
            final Object val = tokens.nextElement();
            if (val instanceof ICell) {
                fieldIdx = ((ICell) val).getColumnIndex(); // hack: gaps possible
            } else {
                fieldIdx++;
            }
            fieldNames.put(fieldIdx, val == null ? null : String.valueOf(val));
            if (fieldIdx > maxFieldIdx) maxFieldIdx = fieldIdx;
        }
        String[] fieldNamesArr = new String[maxFieldIdx+1];
        for(int i=0;i<=maxFieldIdx;i++) {
            fieldNamesArr[i] = fieldNames.get(i);
        }
        setFieldNames(fieldNamesArr);
    }
}
