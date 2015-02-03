package com.agimatec.commons.config;

import java.util.List;
import java.util.Map;

/**
 * Abstract superclass of config nodes that can contain more nodes
 * and atomar nodes, with direct access methods for the atomar values.
 *
 * @author Roman Stumm, Sven Ludwig
 */
public abstract class CompositeNode extends Node {

    /*
    * @param path - a single config-name or "/"-separated config-path
    * get the node specified by the given path
    */
    public Object getNode(final String path) {
        final java.util.StringTokenizer tokens = new java.util.StringTokenizer(path, "/");
        return evaluatePath(tokens, this);
    }

    /**
     * @param name - a single name or concatenated config-name-path.
     * @return the int value of the underlying IntNode or Integer. if the int does not exist, return 0
     */
    public int getInt(final String name) {
        return getInt(name, 0);
    }


    /**
     * @param name         - a single name or concatenated config-name-path.
     * @param defaultValue - value that should be returned in case no node is found with given name
     * @return the int value of the underlying IntNode or Integer. if the int does not exist, return defaultValue
     */
    public int getInt(final String name, final int defaultValue) {
        final Object node = getNode(name);
        if (node == null) return defaultValue;
        if (node instanceof IntNode) {
            return ((IntNode) node).getValue();
        } else {
            return ((Integer) node).intValue();
        }
    }


    /**
     * @param name - a single name or concatenated config-name-path.
     * @return the long value of the LongNode or Long. if the long does not exists, return 0L
     */
    public long getLong(final String name) {
        final Object node = getNode(name);
        if (node == null) return 0L;
        if (node instanceof LongNode) {
            return ((LongNode) node).getValue();
        } else {
            return ((Long) node).longValue();
        }
    }

    /**
     * @param name - a single name or concatenated config-name-path.
     * @return the double value of the DoubleNode or Double. if the double does not exist, return 0.0
     */
    public double getDouble(final String name) {
        final Object node = getNode(name);
        if (node == null) return 0.0;
        if (node instanceof DoubleNode) {
            return ((DoubleNode) node).getValue();
        } else {
            return ((Double) node).doubleValue();
        }
    }

    /**
     * @param name - a single name or concatenated config-name-path.
     * @return String value of the underlying String or TextNode or null. if the string does not exist, return null.
     */
    public String getString(final String name) {
        final Object node = this.getNode(name);
        if (node instanceof TextNode) {
            return ((TextNode) node).getValue();
        } else {
            return (String) node;
        }
    }

    /**
     * @param name - a single name or concatenated config-name-path.
     * @return the boolean value of the underlying BooleanNode or Boolean. if the boolean does not exist, return false.
     */
    public boolean getBoolean(final String name) {
        final Object node = getNode(name);
        if (node == null) return false;
        if (node instanceof BooleanNode) {
            return ((BooleanNode) node).getValue();
        } else {
            return ((Boolean) node).booleanValue();
        }
    }

    /**
     * @param name - a single name or concatenated config-name-path.
     * @return the getPath() of the FileNode under the given name. if the file-node does not exist, return null.
     */
    public String getURLPath(final String name) {
        final FileNode fileNode = (FileNode) getNode(name);
        if (fileNode == null) return null;
        return fileNode.getURLPath();
    }

    public String getFilePath(final String name) {
        final FileNode fileNode = (FileNode) getNode(name);
        if (fileNode == null) return null;
        return fileNode.getFilePath();
    }

    /**
     * @param path - the path to the list or listnode
     * @return return the list or null if the list does not exist
     */
    public List getList(final String path) {
        final Object node = getNode(path);
        if (node == null) return null;
        if (node instanceof List) {
            return (List) node;
        } else {
            return ((ListNode) node).getList();
        }
    }

    /**
     * @param path - path leading to a Map or MapNode
     * @return the Map or null if the Map does not exist.
     */
    public Map getMap(final String path) {
        final Object node = getNode(path);
        if (node == null) return null;
        if (node instanceof Map) {
            return (Map) node;
        } else {
            return ((MapNode) node).getMap();
        }

    }
}
