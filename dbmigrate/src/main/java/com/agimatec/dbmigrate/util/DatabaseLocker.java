package com.agimatec.dbmigrate.util;

import com.agimatec.jdbc.JdbcDatabase;

/**
 * Description: <br>
 * <p>
 * Date: 03.06.15<br>
 * </p>
 */
public interface DatabaseLocker {
    boolean isEnabled();
    void lock(JdbcDatabase database);
    void unlock(JdbcDatabase database);
}
