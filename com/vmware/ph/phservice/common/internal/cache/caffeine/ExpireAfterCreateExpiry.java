package com.vmware.ph.phservice.common.internal.cache.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import java.time.Duration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ExpireAfterCreateExpiry<K, V> implements Expiry<K, V> {
  private static final Log _log = LogFactory.getLog(ExpireAfterCreateExpiry.class);
  
  public long expireAfterCreate(K key, V value, long currentTime) {
    Duration expireAfterCreateDuration = getDuration(key, value);
    if (_log.isInfoEnabled()) {
      String cacheKey = keyToString(key);
      long cacheDurationMillis = expireAfterCreateDuration.toMillis();
      _log.info("Cache entry expiration set to: " + cacheDurationMillis + " millis for key: " + cacheKey);
    } 
    return expireAfterCreateDuration.toNanos();
  }
  
  public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
    return currentDuration;
  }
  
  public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
    return currentDuration;
  }
  
  protected String keyToString(K key) {
    return key.toString();
  }
  
  protected abstract Duration getDuration(K paramK, V paramV);
}
