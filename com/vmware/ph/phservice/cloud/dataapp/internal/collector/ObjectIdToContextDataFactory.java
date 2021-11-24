package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import java.util.Map;

public interface ObjectIdToContextDataFactory {
  Map<String, Object> getObjectIdToContextData(String paramString);
}
