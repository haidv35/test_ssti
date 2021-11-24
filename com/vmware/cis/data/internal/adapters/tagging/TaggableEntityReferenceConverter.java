package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.vapi.std.DynamicID;

public interface TaggableEntityReferenceConverter {
  DynamicID convertComparableValue(Object paramObject);
  
  Object convertPropertyValue(DynamicID paramDynamicID, String paramString);
}
