package com.vmware.cis.data.internal.provider.profiler;

import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.provider.DataProvider;

public class DataProviderProfilerFactory {
  public static DataProvider profileProvider(DataProvider provider, long operationThreshold) {
    assert operationThreshold >= 0L;
    return ProfiledDataProvider.create(new OperationThresholdDataProvider(provider, operationThreshold));
  }
  
  public static DataProviderConnector profileConnector(DataProviderConnector connector, OperationThresholdConfig thresholdConfig) {
    assert thresholdConfig != null;
    return new ProfiledDataProviderConnector(new OperationThresholdDataProviderConnector(connector, thresholdConfig));
  }
}
