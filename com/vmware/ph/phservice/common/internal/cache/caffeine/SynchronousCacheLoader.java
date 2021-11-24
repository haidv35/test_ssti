package com.vmware.ph.phservice.common.internal.cache.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public interface SynchronousCacheLoader<K, V> extends CacheLoader<K, V> {
  default CompletableFuture<V> asyncReload(K key, V oldValue, Executor executor) {
    try {
      return CompletableFuture.completedFuture((V)reload(key, oldValue));
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new CompletionException(e);
    } 
  }
}
