package com.vmware.ph.phservice.provider.common.vmomi.mo;

import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import java.util.ArrayList;
import java.util.List;

public final class MethodNameToQueryPropertyConverter {
  private static final String NOT_METHOD_QUERY_PROPERTY_ERR_MSG = "The given argument [%s] is not a method property.";
  
  private static final String METHOD_QUERY_PROPERTY_PREFIX = "m_";
  
  private static final int METHOD_PROPERTY_PREFIX_LENGTH = "m_"
    .length();
  
  public static String toMethodQueryProperty(String methodName) {
    return "m_" + methodName;
  }
  
  public static List<String> toMethodNames(List<String> queryProperties) {
    List<String> methodNames = new ArrayList<>(queryProperties.size());
    for (String queryProperty : queryProperties) {
      if (!QuerySchemaUtil.isQueryPropertyModelKey(queryProperty))
        methodNames.add(toMethodName(queryProperty)); 
    } 
    return methodNames;
  }
  
  public static String toMethodName(String methodQueryProperty) {
    if (!isMethodQueryProperty(methodQueryProperty))
      throw new IllegalArgumentException(
          String.format("The given argument [%s] is not a method property.", new Object[] { methodQueryProperty })); 
    return methodQueryProperty.substring(METHOD_PROPERTY_PREFIX_LENGTH);
  }
  
  public static boolean isMethodQueryProperty(String property) {
    return (property.startsWith("m_") && property
      .length() > METHOD_PROPERTY_PREFIX_LENGTH);
  }
}
