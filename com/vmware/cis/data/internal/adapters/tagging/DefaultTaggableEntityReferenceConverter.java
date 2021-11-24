package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.vapi.std.DynamicID;

public final class DefaultTaggableEntityReferenceConverter implements TaggableEntityReferenceConverter {
  public static final TaggableEntityReferenceConverter DEFAULT_CONVERTER = new DefaultTaggableEntityReferenceConverter();
  
  public DynamicID convertComparableValue(Object comparableValue) {
    if (comparableValue instanceof DynamicID)
      return (DynamicID)comparableValue; 
    throw new IllegalArgumentException(
        String.format("Invalid comparable value for property 'entity' - expected %s but found %s", new Object[] { DynamicID.class, comparableValue.getClass() }));
  }
  
  public Object convertPropertyValue(DynamicID propertyValue, String serverGuid) {
    return propertyValue;
  }
}
