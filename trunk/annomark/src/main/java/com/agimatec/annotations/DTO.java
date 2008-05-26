package com.agimatec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.TYPE})
public @interface DTO {
     /** symbolic name of the use-case (when multiple DTO annotations exist per class) */
    String usage() default "";

    /** name of DTO class to generate (w/o package!) */
    String dtoClass() default "";

    /** package of DTO-class's package */
    String dtoPackage() default "";
}