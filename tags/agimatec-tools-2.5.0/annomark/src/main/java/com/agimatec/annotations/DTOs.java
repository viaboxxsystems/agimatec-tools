package com.agimatec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 15:59:54 <br/>
 * Copyright: Agimatec GmbH
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.TYPE})
public @interface DTOs {
    DTO[] value();
}
