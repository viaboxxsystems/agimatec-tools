package com.agimatec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 06.12.2007 <br/>
 * Time: 12:08:55 <br/>
 * Copyright: Agimatec GmbH
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.FIELD})
public @interface Default {
    String value();
}
