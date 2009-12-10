package com.agimatec.commons.beans;

import java.io.Serializable;
import java.util.*;

/**
 * <b>Description:</b>   Utility class to query properties and conditions by a simple query syntax.
 * the properties reside in a map.
 * <pre>Example:
 * MapQuery q = new MapQuery();
 * q.parse("platform=TEST");
 * <br/>
 * Map map = new HashMap();
 * map.put("platform", "TEST");
 * map.put("env", "test");
 * <br/>
 * assertTrue(q.doesMatch(map));
 * <br/>
 * map.put("platform", "TEST2");
 * assertTrue(!q.doesMatch(map));
 * <br/>
 * q.parse("platform = TEST2 & env=test ");
 * assertTrue(q.doesMatch(map));
 * </pre>
 * Supports != = & |
 * <p/>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Agimatec GmbH<br>
 *
 * @author Roman Stumm
 */
public final class MapQuery implements Serializable {
    private List transformations = new ArrayList();

    public MapQuery(String queryExpression) {
        parse(queryExpression);
    }

    public MapQuery() {
    }

    /**
     * @param trans
     */
    private void add(QueryOperation trans) {
        transformations.add(trans);
    }

    /**
     * @param op
     */
    private void addOp(String op) {
        transformations.add(op);
    }

    public void parse(String queryExpression) {
        parse(new StringTokenizer(queryExpression, "\t &|!=", true));
    }

    /**
     * @param tokens
     */
    public void parse(StringTokenizer tokens) {
        reset();

        do {
            QueryOperation qop = new QueryOperation();
            add(qop);

            qop.queryAttr = nextNonBlank(tokens);
            qop.operator = checkOperator(tokens, "");
            qop.queryValue = nextNonBlank(tokens);

            String logical = nextNonBlank(tokens);
            if (logical == null) break;

            if ("&".equals(logical)) {
                addOp(logical);
            } else if ("|".equals(logical)) {
                addOp(logical);
            }
        } while (true);
    }

    private void reset() {
        if (transformations == null) {
            transformations = new ArrayList();
        } else {
            transformations.clear();
        }
    }

    private String nextNonBlank(StringTokenizer tokens) {
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (!isBlank(token)) return token;
        }
        return null;
    }

    private boolean isBlank(String aToken) {
        return (" ".equals(aToken) || "\t".equals(aToken));
    }

    /**
     * @param each
     * @return
     */
    public boolean doesMatch(Map each) {
        Iterator iter = transformations.iterator();
        if (!iter.hasNext()) {
            return true;    // empty query, always true
        }

        boolean result;
        String logical;
        QueryOperation qop = (QueryOperation) iter.next();
        result = qop.doesMatch(each);

        while (iter.hasNext()) {
            logical = (String) iter.next();
            qop = (QueryOperation) iter.next();

            boolean nextResult = qop.doesMatch(each);
            if ("&".equals(logical)) {
                result &= nextResult;
            }

            if ("|".equals(logical)) {
                result |= nextResult;
            }
        }

        return result;
    }

    /**
     * != , =
     *
     * @param tokens
     * @param op
     * @return the valid operator list
     */
    private String checkOperator(StringTokenizer tokens, String op) {
        boolean isValid = false;
        String newOp = nextNonBlank(tokens);
        if (newOp != null) {
            op += newOp;

            if ("=".equals(op)) {
                isValid = true;
            } else if ("!=".equals(op)) {
                isValid = true;
            } else if ("!".equals(op)) {
                return checkOperator(tokens, op);
            }
        }

        if (!isValid) {
            throw new UnsupportedOperationException("Invalid operator: " + op);
        }

        return op;
    }

    /**
     * @return
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (Iterator i = transformations.iterator(); i.hasNext();) {
            Object item = i.next();
            buf.append(item.toString());
            buf.append(' ');
        }

        return buf.toString();
    }

}

class QueryOperation implements Serializable {
    protected String queryAttr;
    protected String operator;
    protected String queryValue;

    /**
     * @param each
     * @return
     */
    public boolean doesMatch(Map each) {
        if ("=".equals(operator) &&
                String.valueOf(each.get(queryAttr)).equals(queryValue)) {
            return true;
        }

        if ("!=".equals(operator) &&
                !(String.valueOf(each.get(queryAttr)).equals(queryValue))) {
            return true;
        }

        return false;
    }

    /**
     * @return
     */
    public String toString() {
        return queryAttr + operator + queryValue;
    }

}

