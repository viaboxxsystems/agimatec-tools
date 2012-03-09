package com.agimatec.utility.fileimport;

import java.io.*;

/**
 * Description: read a file line by line (each line is a string)<br/>
 * User: roman.stumm <br/>
 * Date: 11.06.2008 <br/>
 * Time: 17:43:00 <br/>
 * Copyright: Agimatec GmbH
 */
public class StringLineReader implements LineReader<String> {
    protected BufferedReader lineReader;

    public void setReader(Reader aReader) {
        lineReader = new BufferedReader(aReader);
    }

    public void setStream(InputStream aReader) throws IOException {
        setReader(new InputStreamReader(aReader));
    }

    public String readLine() throws IOException {
        return lineReader.readLine();
    }

    public void close() throws IOException {
        lineReader.close();
    }
}
