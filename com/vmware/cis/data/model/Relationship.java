package com.vmware.cis.data.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Relationship {
  public static final int MAX_RELATIONHIP_HOP_COUNT = 3;
  
  String[] value();
}
