package com.meinc.commons.application;

public @interface PhoenixApplication {
	String name();
	String namespace() default "";
	String version();
}
