package com.vmware.ph.phservice.collector.internal.manifest.xml.query.data;

import java.util.Arrays;

public class PropertySpec {
  public String[] propertyNames;
  
  public String toString() {
    return "[propertyNames: " + Arrays.toString((Object[])this.propertyNames) + "]";
  }
}
