package com.meinc.mrsoa.service.annotation;

import java.lang.annotation.Target;

import com.meinc.mrsoa.service.IMrSoaProxy;

/**
 * Specifies a single service or interface that is required or depended upon.
 * Name, interface, and proxy are all mutually exclusive.
 * 
 * @author mpontius
 */
@Target({})
public @interface OnService {
  /** The name of the service required/depended-upon */
  String name() default "";
  /** The namespace of the name or interface required/depended-upon */
  String namespace() default "";
  /** The version of the name or interface required/depended-upon */
  String version() default "";
  /** The name of the interface that is required/depended-upon */
  String interrface() default "";
  /** References an {@link IMrSoaProxy} class that provides the required/depended-upon service */
  Class proxy() default Object.class;
}
