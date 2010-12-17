package com.agimatec.tools.nls.output;

import com.agimatec.commons.util.FileUtils;
import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.model.JSONValue;

import java.io.File;
import java.io.Writer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:29:50 <br/>
 * Copyright: Agimatec GmbH
 */
public class MBJSONPersistencer  {
    private final boolean pretty;

    public MBJSONPersistencer(boolean pretty) {
        this.pretty = pretty;
    }

    public void save(Object object, File file) throws Exception {
        Writer writer = FileUtils.openFileWriterUTF8(file);
        try {
            JSONValue json = JSONMapper.toJSON(object);
            writer.write(json.render(pretty));
        } finally {
            writer.close();
        }
    }
}
