package com.vmware.cis.data.internal.util;

import org.apache.commons.lang.Validate;

public final class UnqualifiedProperty {
  public static String getRootProperty(String unqualifiedPropertyPath) {
    Validate.notEmpty(unqualifiedPropertyPath);
    int firstTokenSeparatorIndex = unqualifiedPropertyPath.indexOf('/');
    if (firstTokenSeparatorIndex == 0)
      throw new IllegalArgumentException(String.format("Property name must not start with '%c': '%s'", new Object[] { Character.valueOf('/'), unqualifiedPropertyPath })); 
    if (firstTokenSeparatorIndex < 0)
      return unqualifiedPropertyPath; 
    return unqualifiedPropertyPath.substring(0, firstTokenSeparatorIndex);
  }
}
