package com.agimatec.commons.config;

import java.util.Map;

/**
 * Description: the root object of a Config file. This class is basically the same as a MapNode.
 *
 * @author Roman Stumm
 */
public class Config extends MapNode {

    public Config() {
    }

    /**
     * create an instance on the given map.
     *
     * @param aMap
     */
    public Config(Map aMap) {
        super(aMap);
    }
}
