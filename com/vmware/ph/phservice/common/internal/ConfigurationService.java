package com.vmware.ph.phservice.common.internal;

public interface ConfigurationService {
  public static final String[] TRUE_VALUES = new String[] { "yes", "true", "on" };
  
  String getProperty(String paramString);
  
  Boolean getBoolProperty(String paramString);
  
  Long getLongProperty(String paramString);
}
