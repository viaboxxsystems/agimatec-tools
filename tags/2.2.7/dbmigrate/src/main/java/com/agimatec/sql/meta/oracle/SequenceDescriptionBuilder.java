package com.agimatec.sql.meta.oracle;

import com.agimatec.sql.meta.SequenceDescription;
import com.agimatec.sql.query.JdbcResultBuilder;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class SequenceDescriptionBuilder implements JdbcResultBuilder {
    private final static int C_SEQUENCE_NAME = 1;
    private final static int C_MIN_VALUE = 2;
    private final static int C_MAX_VALUE = 3;
    private final static int C_INCREMENT_BY = 4;
    private final static int C_CYCLE_FLAG = 5;
    private final static int C_ORDER_FLAG = 6;
    private final static int C_CACHE_SIZE = 7;

    private final static String NOMAX_VALUE =
            "999999999999999999999999999"; // hack: even when SEQUENCE is created with NOMAXVALUE, we get this value here

    private List sequences;

    public void afterExecute(ResultSetMetaData data, Object queryObject, List resultList)
            throws SQLException {
        sequences = resultList;
    }

    public void fetch(ResultSet row) throws SQLException {
        SequenceDescription sequence = new SequenceDescription();
        sequence.setSequenceName(row.getString(C_SEQUENCE_NAME));
        String number;
        number = row.getString(C_MAX_VALUE);

        if (row.wasNull() || NOMAX_VALUE.equals(number)) {
            sequence.setNoMaxValue();
        } else {
            sequence.setMaxValue(new BigDecimal(number));
        }
        number = row.getString(C_MIN_VALUE);
        if (row.wasNull() || NOMAX_VALUE
                .equals("1")) { // hack: even when SEQUENCE is created with NOMINVALUE, we get "1" here
            sequence.setNoMinValue();
        } else {
            sequence.setMinValue(new BigDecimal(number));
        }
        sequence.setIncrement(row.getInt(C_INCREMENT_BY));
        sequence.setCache(row.getInt(C_CACHE_SIZE)); // NOT NULL, 0 means NOCACHE
        if (sequence.getCache() == 0) sequence.setNoCache();
        sequence.setCycle("Y".equals(row.getString(C_CYCLE_FLAG))); // Y N
        sequence.setOrder("Y".equals(row.getString(C_ORDER_FLAG))); // Y N
        sequences.add(sequence);
    }

    public void close(boolean isComplete) throws SQLException {
    }
}
