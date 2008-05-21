package com.agimatec.sql.meta.persistence;

import java.io.File;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 17:38:42 <br/>
 * Copyright: Agimatec GmbH
 */
public interface ObjectPersistencer {
    void save(Object obj, File target) throws Exception;
    Object load(File source) throws Exception;
}
