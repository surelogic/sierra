package com.surelogic.sierra.message.srpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * SRPC Services are defined by writing an interface annotated with
 * {@link Service} and defining one or more methods. The parameters and return
 * type of each method should be annotated with {@link XmlRootElement}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
  String version() default "_none_";
}
