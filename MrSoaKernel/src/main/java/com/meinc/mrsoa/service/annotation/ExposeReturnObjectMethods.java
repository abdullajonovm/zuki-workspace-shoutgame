package com.meinc.mrsoa.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface ExposeReturnObjectMethods {
	String exclude() default "";
	String serviceMethodPrefix() default "";
}
