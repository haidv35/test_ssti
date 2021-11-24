package com.vmware.cis.data.provider.registry;

public interface QueryModelRegistry {
  void registerQueryModel(Class<?> paramClass);
  
  void unregisterQueryModel(Class<?> paramClass);
}
