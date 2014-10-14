package com.agimatec.dbmigrate;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 05.04.2007 <br/>
 * Time: 09:34:47 <br/>
 */
public class HaltedException extends RuntimeException
{
    public HaltedException(String message) {
        super(message);
    }

    /**
     * @since 2.5.19
     * @param s
     * @param throwable
     */
    public HaltedException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
