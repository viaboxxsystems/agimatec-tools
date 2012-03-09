package com.agimatec.sql.meta.script;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 17.12.2007 <br/>
 * Time: 15:20:37 <br/>
 * Copyright: Agimatec GmbH
 */
public class RevertableStringTokenizer implements Enumeration {
    private List<String> tokens = new ArrayList();
    private final StringTokenizer original;
    int position = -1;

    public RevertableStringTokenizer(StringTokenizer original) {
        this.original = original;
    }

    public boolean hasMoreElements() {
        return original.hasMoreElements();
    }

    public Object nextElement() {
        return nextToken();
    }

    public String nextToken() {
        if (position < 0 || position >= tokens.size()) {
            position = -1;
            String next = original.nextToken();
            tokens.add(next);
            return next;
        } else {
            return tokens.get(position++);
        }
    }

    public boolean hasMoreTokens() {
        return !(position < 0 || position >= tokens.size()) || original.hasMoreTokens();
    }

    public int getPosition() {
        if (position < 0 || position >= tokens.size()) {
            return tokens.size();
        } else {
            return position;
        }
    }

    public void setPosition(int index) {
        position = index;
    }
}
