package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import java.util.concurrent.Future;

public interface AsyncCollectorDataAppAgent extends CollectorDataAppAgent {
  Future<String> collectAsync(String paramString1, String paramString2, Object paramObject);
}
