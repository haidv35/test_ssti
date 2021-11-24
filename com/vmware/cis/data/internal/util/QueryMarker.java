package com.vmware.cis.data.internal.util;

import org.slf4j.MDC;

public final class QueryMarker {
  public static final String QUERY_CONTROL_INTERNAL = "internal";
  
  public static final String QUERY_CONTROL_EXTERNAL = "external";
  
  private static final String QUERY_ID_KEY = "queryId";
  
  private static final String PARENT_QUERY_ID_KEY = "parentQueryId";
  
  private static final String QUERY_CONTROL_KEY = "queryControl";
  
  private static final String PROVIDER_NAME_KEY = "providerName";
  
  public static void setQueryId(String queryId) {
    if (queryId == null) {
      MDC.remove("queryId");
    } else {
      MDC.put("queryId", queryId);
    } 
  }
  
  public static String getQueryId() {
    return MDC.get("queryId");
  }
  
  public static void setParentQueryId(String queryId) {
    if (queryId == null) {
      MDC.remove("parentQueryId");
    } else {
      MDC.put("parentQueryId", queryId);
    } 
  }
  
  public static String getParentQueryId() {
    return MDC.get("parentQueryId");
  }
  
  public static void setQueryControl(String control) {
    if (control == null) {
      MDC.remove("queryControl");
    } else {
      MDC.put("queryControl", control);
    } 
  }
  
  public static String getQueryControl() {
    return MDC.get("queryControl");
  }
  
  public static void setProviderName(String providerName) {
    if (providerName == null) {
      MDC.remove("providerName");
    } else {
      MDC.put("providerName", providerName);
    } 
  }
  
  public static String getProviderName() {
    return MDC.get("providerName");
  }
}
