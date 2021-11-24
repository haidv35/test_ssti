package com.vmware.ph.phservice.common.internal.cache;

import java.util.Collection;
import java.util.Map;

@Deprecated
public interface Cache<K, V> {
  void put(K paramK, V paramV);
  
  V get(K paramK);
  
  Collection<K> getAllKeys();
  
  Collection<V> getAllValues();
  
  Map<K, V> export();
  
  boolean contains(K paramK);
  
  void invalidate(K paramK);
  
  void invalidate();
}
