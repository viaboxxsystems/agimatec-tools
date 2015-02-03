package com.agimatec.sql.query;

import java.io.Serializable;

/** This object allows to query for any SQL possible that does return plain data. */
public class QueryDefinition implements Serializable {
    public static final int UNLIMITED = 0;
    private String queryName;
    private Object queryObject; // optional

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(final String qsn) {
        queryName = qsn;
    }

    public Object getQueryObject() {
        return queryObject;
    }

    public void setQueryObject(final Object ser) {
        queryObject = ser;
    }

    /**
     * constant to indicate that maxResults should be unlimited,
     * that means that complete results are wanted.
     */
    private int maxResults = UNLIMITED; // optional

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(final int i) {
        maxResults = i;
    }
}
