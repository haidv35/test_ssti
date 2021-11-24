package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import java.util.Arrays;
import java.util.List;

public final class SystemWrapper {
  private static final List<String> permittedKeyRequests = Arrays.asList(new String[] { "os.arch", "os.version", "os.name" });
  
  public static String getProperty(String key) {
    return getProperty(key, null);
  }
  
  public static String getProperty(String key, String def) {
    return permittedKeyRequests.stream()
      .filter(permitted -> permitted.equals(key))
      .map(System::getProperty)
      .findFirst()
      .orElse(def);
  }
}
