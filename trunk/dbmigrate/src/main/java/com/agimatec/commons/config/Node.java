package com.agimatec.commons.config;

import com.agimatec.commons.config.sax.ConfigWriter;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Description: Abstract superclass of all Node-Classes representing Config-elements.
 *
 * @author Roman Stumm
 */
public abstract class Node implements Serializable {

    protected String name;

    public Node() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String aName) {
        name = aName;
    }

    public abstract Object getObjectValue();

    protected Object evaluatePath(final StringTokenizer tokens, Object node) {
        do {
            final String token = tokens.nextToken();
            if (node instanceof MapNode) {
                node = ((MapNode) node).get(token);
            } else if (node instanceof ListNode) {
                node = ((ListNode) node).get(Integer.parseInt(token));
            } else if (node instanceof Map) {
                node = ((Map) node).get(token);
            } else if (node instanceof List) {
                node = ((List) node).get(Integer.parseInt(token));
            } else throw new IllegalArgumentException(
                    "cannot access " + token + " from " + node);
            if (node == null) break;
        } while (tokens.hasMoreTokens());
        return node;
    }

    public String toString() {
        try {
            final StringWriter sw = new java.io.StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            new ConfigWriter().writeConfig(this, pw);
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String toString(String encoding) {
        try {
            final StringWriter sw = new java.io.StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.print("<?xml version=\"1.0\" encoding=\"");
            pw.print(encoding);
            pw.println("\"?>");
            new ConfigWriter().writeConfig(this, pw);
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

