package com.agimatec.sql.meta.script;

import java.util.*;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 *
 * @author Roman Stumm
 */
public final class ExtractExpr extends A_ExtractPart {
    private final String name;
    private final List parts;
    private ExtractSeparator repeatSep;
    private boolean optional;

    // during evaluation:
    private Map parent;

    public ExtractExpr(String aName) {
        name = aName;
        parts = new ArrayList();
        repeatSep = null;
    }

    public ExtractExpr(String aName, String repeatSeparator) {
        name = aName;
        parts = new ArrayList();
        repeatSep = new ExtractSeparator(repeatSeparator);
    }

    public ExtractExpr addWord(String word) {
        add(new ExtractWord(word, false));
        return this;
    }

    public ExtractExpr addOptionalWord(String word) {
        add(new ExtractWord(word, true));
        return this;
    }

    public ExtractExpr addSeparator() {
        add(new ExtractSeparator());
        return this;
    }

    public ExtractExpr addSeparator(String seperator) {
        add(new ExtractSeparator(seperator));
        return this;
    }

    public ExtractExpr addExpr(ExtractExpr expr) {
        add(expr);
        return this;
    }

    /**
     * @param expr
     * @return
     */
    public ExtractExpr addOptionalExpr(ExtractExpr expr) {
        expr.optional = true;
        return addExpr(expr);
    }

    public ExtractExpr addProperty(String prop) {
        return addProperty(null, prop);
    }

    public ExtractExpr addOptionalProperty(String prop) {
        return addOptionalProperty(null, prop);
    }

    public ExtractExpr addOptionalProperty(String word, String propName) {
        add(new ExtractProperty(propName, word, true));
        return this;
    }

    public ExtractExpr addProperty(String word, String propName) {
        add(new ExtractProperty(propName, word, false));
        return this;
    }

    public ExtractExpr addProperty(String word, String propName, boolean isOptional, String startDelim, String endDelim) {
        ExtractProperty prop = new ExtractProperty(propName, word, isOptional);
        prop.setStartDelimeter(startDelim);
        prop.setEndDelimeter(endDelim);
        add(prop);
        return this;
    }

    private void add(A_ExtractPart part) {
        parts.add(part);
    }

    public Iterator parts() {
        return parts.iterator();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (optional) buf.append("[");
        Iterator theParts = parts();
        buf.append("{");
        buf.append(name);
        buf.append(' ');
        while (theParts.hasNext()) {
            A_ExtractPart part = (A_ExtractPart) theParts.next();
            buf.append(part);
            if (theParts.hasNext()) buf.append(" ");
        }
        if (isRepeating()) {
            buf.append("...");
            buf.append(getRepeatSep().toString());
        }
        buf.append('}');
        if (optional) buf.append("]");
        return buf.toString();
    }

    /**
     * @param aToken
     * @return C_NOT_HANDLED
     */
    public int fits(String aToken) {
        return C_NOT_HANDLED;
    }

    public int process(String aToken, PropertiesExtractor extractor) {
        int storeIdx = extractor.getTokens().getPosition();
        parent = extractor.current;
        final int result;
        if (isRepeating()) {
            installRepeats();
            nextRepeatMap(extractor);
            result = (extractor.process(this));
        } else {
            Map map = new HashMap();
            parent.put(name, map);
            extractor.current = map;
            result = (extractor.process(this));
        }
        extractor.current = parent;
        if ((result == C_ERROR || result == C_FIT_NOT) && optional) {
            parent.remove(name);
            extractor.setToken(aToken);
            extractor.getTokens().setPosition(storeIdx);
            return C_NOT_HANDLED;
        } else {
            return (result == C_FIT_NOT) ? C_ERROR : result;
        }
    }

    private void installRepeats() {
        ArrayList repeats = new ArrayList();
        parent.put(name, repeats);
    }

    private Map nextRepeatMap(PropertiesExtractor extractor) {
        Map map = new HashMap();
        extractor.current = map;
        getRepeats().add(map);
        return map;
    }

    private List getRepeats() {
        return (List) parent.get(name);
    }

    boolean isRepeating() {
        return repeatSep != null;
    }

    public ExtractSeparator getRepeatSep() {
        return repeatSep;
    }

    void setRepeatSep(ExtractSeparator aRepeatSep) {
        repeatSep = aRepeatSep;
    }

    public int prepareLoop(String aToken, PropertiesExtractor extractor) {
        int result = getRepeatSep().fits(aToken);
        if (result == C_FIT || result == C_MAY_FIT) {
            nextRepeatMap(extractor);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    protected boolean isOptional() {
        return optional;
    }
}

