package com.agimatec.sql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

final class SQLDateFormats {
    /**
     * <b>NOTE:</b> <code>DateFormat.format()</code> is <b>not</b> tread safe!
     */
    public final DateFormat DateFormYYYYMMDD;

    // timestamp formats
    public final DateFormat TimestampFormYYYYMMDDHHmmss;

    public final DateFormat TimeFormHHMMSS;

    {
        DateFormYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
        DateFormYYYYMMDD.setLenient(false);

        TimestampFormYYYYMMDDHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimestampFormYYYYMMDDHHmmss.setLenient(false);

        TimeFormHHMMSS = new SimpleDateFormat("HH:mm:ss");
        TimeFormHHMMSS.setLenient(false);
    }
}

