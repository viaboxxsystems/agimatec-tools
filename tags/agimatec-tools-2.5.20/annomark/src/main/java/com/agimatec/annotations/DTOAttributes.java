package com.agimatec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 08.06.2007 <br/>
 * Time: 16:00:23 <br/>
 * Copyright: Apache 2.0 License
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.FIELD, ElementType.METHOD})
public @interface DTOAttributes {
    DTOAttribute[] value();
}
