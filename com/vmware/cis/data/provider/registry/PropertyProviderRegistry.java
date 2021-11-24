package com.vmware.cis.data.provider.registry;

public interface PropertyProviderRegistry {
  void register(Object paramObject);
  
  void unregister(Object paramObject);
}
