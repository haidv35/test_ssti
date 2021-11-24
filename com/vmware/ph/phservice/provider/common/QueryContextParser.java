package com.vmware.ph.phservice.provider.common;

public class QueryContextParser {
  public QueryContext parse(Object context) {
    if (context instanceof QueryContext)
      return (QueryContext)context; 
    return null;
  }
}
