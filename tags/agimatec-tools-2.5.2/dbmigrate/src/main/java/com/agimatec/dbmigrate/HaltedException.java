package com.agimatec.dbmigrate;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 05.04.2007 <br/>
 * Time: 09:34:47 <br/>
 * Copyright: Agimatec GmbH
 */
public class HaltedException extends RuntimeException
{
    public HaltedException(String message) {
        super(message);
    }
}
