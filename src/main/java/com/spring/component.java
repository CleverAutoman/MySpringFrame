package com.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)

// the target of thr interface is class and interface
@Target(ElementType.TYPE)
public @interface component {

    // Only Search the file in the target location
    String value() default "";
}
