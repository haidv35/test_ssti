package com.vmware.ph.phservice.common.internal.cache;

public interface CacheInvalidateCallback<V> {
  void cacheValueInvalidated(V paramV);
}
