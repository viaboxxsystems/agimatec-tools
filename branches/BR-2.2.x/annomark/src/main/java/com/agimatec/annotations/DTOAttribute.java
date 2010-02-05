package com.agimatec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(value = {ElementType.FIELD, ElementType.METHOD})
public @interface DTOAttribute {
    /** reference to DTO.usage (when multiple DTO annotations exist per class) */
    String usage() default "";

    /** Reflector.path to take the attribute of the pojo FROM */
    String path() default "";

    /** name of the pojo.property to write the value TO */
    String property() default "";

    /** for dozer-mapping: name of a Class<net.sf.dozer.util.mapping.converters.CustomConverter> */
    String converter() default "";

    /** for dozer-mapping: true when call-by-reference */
    boolean copyByReference() default false;

    /** for dozer-mapping: true when one-way (from annotated to generated) */
    boolean oneWay() default false;

    /** the type of the property, default is the same type as origin's */
    String type() default "";

    /** iterate-type setter method */
    String addMethod() default "";
}