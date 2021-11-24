package com.vmware.ph.phservice.provider.vcenter.performance;

import java.util.Arrays;

public enum PerfInterval {
  REALTIME(0),
  DAILY(1),
  WEEKLY(2),
  MONTHLY(3),
  YEARLY(4);
  
  private final int _key;
  
  PerfInterval(int key) {
    this._key = key;
  }
  
  public int getKey() {
    return this._key;
  }
  
  public static PerfInterval parse(String internalName) {
    for (PerfInterval perfInterval : values()) {
      if (perfInterval.toString().equalsIgnoreCase(internalName))
        return perfInterval; 
    } 
    throw new IllegalArgumentException(
        String.format("Cannot parse string [%s] to PerformanceInterval. Supported interval values are %s.", new Object[] { internalName, Arrays.toString(values()) }));
  }
  
  public String toString() {
    return name().toLowerCase();
  }
}
