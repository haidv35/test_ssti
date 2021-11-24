package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.DataProvider;

public interface DataProviderRegistry {
  void register(DataProvider paramDataProvider);
  
  void unregister(DataProvider paramDataProvider);
}
