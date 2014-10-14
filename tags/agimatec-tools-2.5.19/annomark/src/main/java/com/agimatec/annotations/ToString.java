package com.agimatec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: annotation for a field/getter method
 * that should appear in the toString() implementation of the
 * generated class<br/>
 * User: roman.stumm <br/>
 * Date: 20.06.2008 <br/>
 * Time: 13:27:14 <br/>
 * Copyright: Apache 2.0 License
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.FIELD, ElementType.METHOD})
public @interface ToString {
}
