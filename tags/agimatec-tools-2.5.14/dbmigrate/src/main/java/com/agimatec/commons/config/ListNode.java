/* Copyright 2001 Agimatec GmbH  */

package com.agimatec.commons.config;

import java.util.ArrayList;

/**
 * A config node representing a list of nodes.
 *
 * @author Roman Stumm
 */
public class ListNode extends CompositeNode {

    protected ArrayList list;

    public ListNode() {
        list = new ArrayList();
    }

    public Object getObjectValue() {
        return list;
    }

    public ArrayList getList() {
        return list;
    }

    public Object get(final int index) {
        return getList().get(index);
    }

    public int nodeCount() {
        return getList().size();
    }

    /**
     * cast the result of get(int) to Node
     *
     * @throws ClassCastException when this is not possible
     */
    public Node getNode(final int index) {
        return (Node) getList().get(index);
    }

}

