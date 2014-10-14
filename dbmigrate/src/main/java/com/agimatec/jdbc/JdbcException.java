package com.agimatec.jdbc;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 04.04.2007 <br/>
 * Time: 12:36:41 <br/>
 */
public class JdbcException extends RuntimeException {

    public JdbcException(Throwable cause) {
        super(cause);
    }

    public JdbcException(String cause) {
        super(cause);
    }
}
