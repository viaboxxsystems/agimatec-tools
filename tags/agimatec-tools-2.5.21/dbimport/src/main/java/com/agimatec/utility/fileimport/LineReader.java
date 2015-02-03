package com.agimatec.utility.fileimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Description: Interface of a reader used by {@link LineImporterSpec} to
 * read from a reader or stream record-by-record (line-by-line)<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:42:47 <br/>
 */
public interface LineReader<Line> {
    void setReader(Reader aReader) throws IOException;
    void setStream(InputStream aReader) throws IOException;

    /**
     * @return the next line or null if no more lines available.
     * @throws IOException
     */
    Line readLine() throws IOException;

    void close() throws IOException;
}
