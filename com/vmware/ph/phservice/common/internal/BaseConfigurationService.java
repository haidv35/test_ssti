package com.vmware.ph.phservice.common.internal;

public abstract class BaseConfigurationService implements ConfigurationService {
  public Boolean getBoolProperty(String propertyName) {
    String rawValue = getProperty(propertyName);
    if (rawValue == null)
      return null; 
    String lcRawValue = rawValue.trim().toLowerCase();
    for (String trueValue : TRUE_VALUES) {
      if (trueValue.equals(lcRawValue))
        return Boolean.valueOf(true); 
    } 
    return Boolean.valueOf(false);
  }
  
  public Long getLongProperty(String propertyName) {
    String rawValue = getProperty(propertyName);
    if (rawValue == null)
      return null; 
    try {
      return Long.valueOf(Long.parseLong(rawValue));
    } catch (NumberFormatException e) {
      return null;
    } 
  }
}
