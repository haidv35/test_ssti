package com.vmware.ph.phservice.common.vapi;

import com.vmware.vapi.bindings.type.StructType;

public interface VapiTypeProvider {
  StructType getResourceModelType(String paramString);
}
