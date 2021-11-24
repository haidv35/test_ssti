package com.vmware.ph.phservice.common.internal;

import java.util.regex.Pattern;

public class IdFormatUtil {
  private static final String COLLECTOR_INSTANCE_ID_REGEX = "[\\w-]{1,64}";
  
  private static final Pattern COLLECTOR_INSTANCE_ID_PATTERN = Pattern.compile("[\\w-]{1,64}");
  
  private static final String COLLECTOR_ID_REGEX = "[a-zA-Z][\\w-\\.]{1,40}[a-zA-Z0-9]";
  
  private static final Pattern COLLECTOR_ID_PATTERN = Pattern.compile("[a-zA-Z][\\w-\\.]{1,40}[a-zA-Z0-9]");
  
  public static boolean isValidCollectorInstanceId(String collectorInstanceId) {
    return (collectorInstanceId != null && COLLECTOR_INSTANCE_ID_PATTERN
      .matcher(collectorInstanceId).matches());
  }
  
  public static boolean isValidCollectorId(String collectorId) {
    return (collectorId != null && COLLECTOR_ID_PATTERN
      .matcher(collectorId).matches());
  }
}
