package com.agimatec.utility.fileimport;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * SqlUtil Tester.
 *
 * @author ${USER}
 * @version 1.0
 * @since <pre>08/30/2007</pre>
 */
public class SqlUtilTest extends TestCase {
    public SqlUtilTest(String name) {
        super(name);
    }

    public void testSqlUtil() throws Exception {
        SqlUtil util = SqlUtil.forPostgres();
        util.defSequence("id", "SEQ_norge_import");
        util.defDate("DateFrom", "yyyy-MM-dd");
        assertEquals("nextval('SEQ_norge_import')", util.get("id"));
        Date dt =  new java.sql.Date(createDate(2007, 12, 30).getTime());
        assertEquals(dt, util.date("DateFrom", " 2007-12-30  "));
        assertEquals(null, util.date("DateFrom", ""));
        assertEquals(null, util.date("DateFrom", null));

        util = SqlUtil.forOracle();
        util.defSequence("id", "SEQ_norge_import");
        assertEquals("SEQ_norge_import.NEXTVAL", util.get("id"));
    }

    public void testNumber() throws ParseException {
        SqlUtil util = SqlUtil.forPostgres();
        util.defNumber("size", "#");
        Number num = util.number("size", "0013500");
        assertEquals(13500, num.intValue());
    }

    public Date createDate(int year, int month, int day) {
        Calendar gc = Calendar.getInstance();
        gc.clear();
        gc.set(year, month - 1, day);
        return new Date(gc.getTimeInMillis());
    }

    public static Test suite() {
        return new TestSuite(SqlUtilTest.class);
    }
}
