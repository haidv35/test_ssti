package com.vmware.ph.phservice.common.manifest;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.annotations.VisibleForTesting;
import com.vmware.ph.phservice.common.Pair;
import java.time.Duration;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CachedManifestContentProviderWrapper implements ManifestContentProvider {
  private static final Log log = LogFactory.getLog(CachedManifestContentProviderWrapper.class);
  
  private static final int MANIFEST_CONTENT_CACHE_CAPACITY = 1;
  
  private static final Duration DURATION_FOREVER = Duration.ofNanos(Long.MAX_VALUE);
  
  private final ManifestContentProvider _wrappedManifestContentProvider;
  
  private final Cache<ManifestKey, ManifestValue> _manifestContentCache;
  
  private final MaxAgeParser _maxAgeParser;
  
  private final Duration _defaultCacheDuration;
  
  public CachedManifestContentProviderWrapper(ManifestContentProvider wrappedManifestContentProvider, long cacheExpirationMillis) {
    this._maxAgeParser = new MaxAgeParser();
    this._wrappedManifestContentProvider = wrappedManifestContentProvider;
    this._defaultCacheDuration = Duration.ofMillis(cacheExpirationMillis);
    this._manifestContentCache = createManifestContentCache();
  }
  
  public boolean isEnabled() {
    return this._wrappedManifestContentProvider.isEnabled();
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) {
    ManifestKey manifestKey = new ManifestKey(collectorId, collectorInstanceId);
    ManifestValue oldValue = getAndInvalidate(manifestKey);
    Optional<ManifestValue> manifestValue = Optional.ofNullable(this._manifestContentCache
        .get(manifestKey, key -> load(key, oldValue)));
    if (log.isDebugEnabled())
      log.debug("Cache stats: " + this._manifestContentCache.stats()); 
    return manifestValue.<String>map(ManifestValue::getValue).orElse(null);
  }
  
  private ManifestValue load(ManifestKey key, ManifestValue oldValue) {
    try {
      if (log.isDebugEnabled())
        log.debug("Loading manifest with key " + key); 
      String manifestContent = this._wrappedManifestContentProvider.getManifestContent(key.getCollectorId(), key.getCollectorInstanceId());
      if (log.isTraceEnabled())
        log.trace("Loaded manifest: \n" + manifestContent); 
      Duration maxAge = this._maxAgeParser.parseDuration(manifestContent).orElse(this._defaultCacheDuration);
      if (log.isInfoEnabled())
        log.info(String.format("Caching manifest with key: %s for: %d minutes.", new Object[] { key, Long.valueOf(maxAge.toMinutes()) })); 
      return new ManifestValue(manifestContent, maxAge);
    } catch (ManifestException e) {
      log.warn("Error while retrieving the collector manifest. ", e);
      if (oldValue != null) {
        if (log.isInfoEnabled())
          log.info(String.format("Previously cached manifest is present. Will use it and extend it's time-to-live by %s minutes in order to remedy previous errors.", new Object[] { Long.valueOf(this._defaultCacheDuration.toMinutes()) })); 
      } else {
        log.warn(String.format("Previously cached manifest is not available. Will cache an empty value for %s minutes and retry again once it has expired.", new Object[] { Long.valueOf(this._defaultCacheDuration.toMinutes()) }));
      } 
      return new ManifestValue(
          Optional.<ManifestValue>ofNullable(oldValue).map(ManifestValue::getValue).orElse(null), this._defaultCacheDuration);
    } 
  }
  
  private ManifestValue getAndInvalidate(ManifestKey manifestKey) {
    ManifestValue cachedManifest = (ManifestValue)this._manifestContentCache.getIfPresent(manifestKey);
    if (cachedManifest != null) {
      Duration cachedManifestExpireAfter = cachedManifest.getExpireAfter();
      Duration cachedManifestAge = this._manifestContentCache.policy().expireAfterWrite().flatMap(expiration -> expiration.ageOf(manifestKey)).orElse(Duration.ZERO);
      if (cachedManifestAge.compareTo(cachedManifestExpireAfter) > 0) {
        if (log.isDebugEnabled())
          log.debug(String.format("Invalidate cached manifest for %s, it's %d min. old which is older than the allowed %d min.", new Object[] { manifestKey, 
                  
                  Long.valueOf(cachedManifestAge.toMinutes()), Long.valueOf(cachedManifestExpireAfter.toMinutes()) })); 
        this._manifestContentCache.invalidate(manifestKey);
      } 
    } 
    return cachedManifest;
  }
  
  private Cache<ManifestKey, ManifestValue> createManifestContentCache() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder().ticker(createCacheTicker()).maximumSize(1L).expireAfterWrite(DURATION_FOREVER);
    if (log.isDebugEnabled()) {
      cacheBuilder.recordStats();
      cacheBuilder.removalListener((manifestKey, telemetryLevelInfo, removalCause) -> log.debug(String.format("(%s) Removing cached manifest for %s", new Object[] { removalCause, manifestKey })));
    } 
    return cacheBuilder.build();
  }
  
  @VisibleForTesting
  protected Ticker createCacheTicker() {
    return Ticker.systemTicker();
  }
  
  @VisibleForTesting
  public void invalidateCache() {
    this._manifestContentCache.invalidateAll();
  }
  
  private static class ManifestKey extends Pair<String, String> {
    ManifestKey(String collectorId, String collectorInstanceId) {
      super(collectorId, collectorInstanceId);
    }
    
    String getCollectorId() {
      return getFirst();
    }
    
    String getCollectorInstanceId() {
      return getSecond();
    }
  }
  
  private static class ManifestValue extends Pair<String, Duration> {
    ManifestValue(String value, Duration expireAfter) {
      super(value, expireAfter);
    }
    
    String getValue() {
      return getFirst();
    }
    
    Duration getExpireAfter() {
      return getSecond();
    }
  }
}
