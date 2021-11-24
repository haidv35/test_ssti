package com.vmware.cis.data.internal.provider.ext.alias;

final class AliasLengthProperty {
  static final String SUFFIX_LENGTH = "/length";
  
  static String resolveSuffix(String property) {
    if (property == null)
      return null; 
    if (property.endsWith("/length"))
      return "/length"; 
    return null;
  }
  
  static String cleanSuffix(String property, String suffix) {
    if (property == null || suffix == null)
      return property; 
    if (property.endsWith(suffix))
      return property.substring(0, property.length() - suffix.length()); 
    return property;
  }
  
  static String appendSuffix(String property, String suffix) {
    if (property == null || suffix == null)
      return property; 
    return property + suffix;
  }
}
