package com.meinc.mrsoa.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Service {
	String name();
	String namespace() default "";
	String interfaces();
	String version() default "";
	Class exposeAs() default Object.class;
}
