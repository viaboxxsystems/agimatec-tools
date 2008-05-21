package com.agimatec.dbhistory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 18:15:27 <br/>
 * Copyright: Agimatec GmbH
 */
@XStreamAlias("historyConfig")
public class HistSchemaConfig implements Serializable {
    @XStreamImplicit
    private final List<HistTableConfig> tables = new ArrayList();

    public List<HistTableConfig> getTables() {
        return tables;
    }

    public void addTableConfig(HistTableConfig tableConfig)
    {
        tables.add(tableConfig);
    }
}
