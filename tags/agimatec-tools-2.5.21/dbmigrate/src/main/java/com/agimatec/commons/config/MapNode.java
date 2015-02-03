package com.agimatec.commons.config;

import java.util.HashMap;
import java.util.Map;

/**
 * A config node representing a map with nodes.
 * developed: 2001
 *
 * @author Roman Stumm
 */
public class MapNode extends CompositeNode {

    protected final Map map;

    /**
     * create an instance with a new Map
     */
    public MapNode() {
        map = new HashMap();
    }

    /**
     * create an instance on the given map
     *
     * @param aMap
     */
    public MapNode(Map aMap) {
        if(aMap == null) throw new NullPointerException();
        map = aMap;
    }

    /**
     * @return a Map
     */
    public Object getObjectValue() {
        return map;
    }

    /**
     * @return a Map
     */
    public Map getMap() {
        return map;
    }

    /**
     * get a value from the receiver's map
     *
     * @param key - a key in this MapNode's map
     * @return the value or null (if not found)
     */
    public Object get(final Object key) {
        return getMap().get(key);
    }

    /**
     * put a value into the receiver's map
     *
     * @param key
     * @param value
     */
    public void put(final Object key, final Object value) {
        getMap().put(key, value);
    }

    /**
     * put a named Node into the receiver's map
     *
     * @param node - needs a name
     */
    public void put(Node node) {
        getMap().put(node.getName(), node);
    }
}

