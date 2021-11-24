package com.vmware.ph.phservice.push.telemetry;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.annotations.VisibleForTesting;
import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.internal.cache.caffeine.ExpireAfterCreateExpiry;
import com.vmware.ph.phservice.common.internal.cache.caffeine.SynchronousCacheLoader;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.common.manifest.MaxAgeParser;
import com.vmware.ph.phservice.push.telemetry.internal.xml.TelemetryXmlManifestUtils;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTelemetryLevelService implements TelemetryLevelService {
  private static final Logger _log = LoggerFactory.getLogger(DefaultTelemetryLevelService.class);
  
  private static final TelemetryLevel DEFAULT_TELEMETRY_LEVEL = TelemetryLevel.FULL;
  
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final ManifestContentProvider _manifestContentProvider;
  
  private final LoadingCache<CollectorAgent, TelemetryLevelInfo> _telemetryLevelInfoCache;
  
  private final MaxAgeParser _maxAgeParser;
  
  public DefaultTelemetryLevelService(CeipConfigProvider ceipConfigProvider, ManifestContentProvider manifestContentProvider, long cacheExpirationIntervalMillis, long cacheRefreshIntervalMillis, int cacheCapacity) {
    this._ceipConfigProvider = ceipConfigProvider;
    this._manifestContentProvider = manifestContentProvider;
    this._telemetryLevelInfoCache = createTelemetryLevelInfoCache(cacheExpirationIntervalMillis, cacheRefreshIntervalMillis, cacheCapacity);
    this._maxAgeParser = new MaxAgeParser();
  }
  
  public TelemetryLevel getTelemetryLevel(String collectorId, String collectorInstanceId) {
    boolean isCeipEnabled = false;
    if (this._ceipConfigProvider != null)
      isCeipEnabled = this._ceipConfigProvider.isCeipEnabled(); 
    CollectorAgent collectorAgent = new CollectorAgent(collectorId, collectorInstanceId);
    if (!isCeipEnabled) {
      if (_log.isDebugEnabled())
        _log.debug("CEIP is disabled. Telemetry level is evaluated to OFF for {}", collectorAgent); 
      return TelemetryLevel.OFF;
    } 
    if (this._manifestContentProvider == null) {
      _log.info("There is no manifest content provider. Using default telemetry level {}", DEFAULT_TELEMETRY_LEVEL);
      return DEFAULT_TELEMETRY_LEVEL;
    } 
    return getTelemetryLevelFromCache(collectorAgent);
  }
  
  private TelemetryLevel getTelemetryLevelFromCache(CollectorAgent collectorAgent) {
    TelemetryLevel telemetryLevel = DEFAULT_TELEMETRY_LEVEL;
    try {
      TelemetryLevelInfo telemetryLevelInfo = (TelemetryLevelInfo)this._telemetryLevelInfoCache.get(collectorAgent);
      if (telemetryLevelInfo != null) {
        telemetryLevel = telemetryLevelInfo.getTelemetryLevel();
      } else {
        _log.warn("No cache entry available for {}", collectorAgent);
      } 
    } catch (Exception e) {
      _log.error("Unexpected error during telemetry level retrieval for {}", collectorAgent, e);
    } 
    if (_log.isDebugEnabled()) {
      _log.debug("Returning telemetry level {} for {}", telemetryLevel, collectorAgent);
      _log.debug("Cache stats: {}", this._telemetryLevelInfoCache.stats());
    } 
    return telemetryLevel;
  }
  
  private TelemetryLevelInfo getTelemetryLevelInfo(CollectorAgent collectorAgent) throws ManifestContentProvider.ManifestException {
    TelemetryLevel telemetryLevel;
    Duration maxAgeDuration = null;
    try {
      String manifestContent = this._manifestContentProvider.getManifestContent(collectorAgent
          .getCollectorId(), collectorAgent
          .getCollectorInstanceId());
      if (_log.isDebugEnabled()) {
        _log.debug("Retrieved manifest for {}. Enable 'trace' level logging to see the manifest content.", collectorAgent
            
            .getCollectorId());
      } else if (_log.isTraceEnabled()) {
        _log.trace("Manifest content for {}:\n {}", collectorAgent, StringUtils.abbreviate(manifestContent, 1000));
      } 
      String telemetryLevelValue = TelemetryXmlManifestUtils.getTelemetryLevel(manifestContent);
      telemetryLevel = getTelemetryLevelFromStringValue(telemetryLevelValue);
      maxAgeDuration = this._maxAgeParser.parseDuration(manifestContent).orElse(null);
    } catch (com.vmware.ph.phservice.common.manifest.ManifestContentProvider.ManifestException e) {
      telemetryLevel = getTelemetryLevelForFailedManifestRetrieval(collectorAgent, e);
    } 
    return new TelemetryLevelInfo(telemetryLevel, maxAgeDuration);
  }
  
  private static TelemetryLevel getTelemetryLevelForFailedManifestRetrieval(CollectorAgent collectorAgent, ManifestContentProvider.ManifestException manifestException) throws ManifestContentProvider.ManifestException {
    ManifestContentProvider.ManifestExceptionType manifestExceptionType = manifestException.getManifestExceptionType();
    switch (manifestExceptionType) {
      case MANIFEST_NOT_FOUND_ERROR:
        if (_log.isDebugEnabled())
          _log.debug("Manifest not found for {}", collectorAgent); 
        return DEFAULT_TELEMETRY_LEVEL;
      case INVALID_COLLECTOR_ERROR:
        if (_log.isDebugEnabled()) {
          _log.debug("Invalid collector error for {}", collectorAgent, manifestException);
        } else {
          _log.warn("Invalid collector error for {}. Enable 'debug' level logging to see the stack trace.", collectorAgent);
        } 
        return TelemetryLevel.OFF;
    } 
    throw manifestException;
  }
  
  private static TelemetryLevel getTelemetryLevelFromStringValue(String telemetryLevelValue) {
    TelemetryLevel telemetryLevel = DEFAULT_TELEMETRY_LEVEL;
    if (!StringUtils.isEmpty(telemetryLevelValue))
      try {
        telemetryLevel = TelemetryLevel.valueOf(telemetryLevelValue.toUpperCase());
      } catch (IllegalArgumentException e) {
        if (_log.isDebugEnabled())
          _log.debug("Invalid telemetry value provided {}", telemetryLevelValue, e); 
      }  
    return telemetryLevel;
  }
  
  private LoadingCache<CollectorAgent, TelemetryLevelInfo> createTelemetryLevelInfoCache(long cacheExpirationIntervalMillis, long cacheRefreshIntervalMillis, int cacheCapacity) {
    final Duration cacheExpirationDuration = Duration.ofMillis(cacheExpirationIntervalMillis);
    Caffeine<CollectorAgent, TelemetryLevelInfo> cacheBuilder = Caffeine.newBuilder().softValues().maximumSize(cacheCapacity).ticker(createCacheTicker()).expireAfter(new ExpireAfterCreateExpiry<CollectorAgent, TelemetryLevelInfo>() {
          protected Duration getDuration(CollectorAgent collectorAgent, DefaultTelemetryLevelService.TelemetryLevelInfo telemetryLevelInfo) {
            return Optional.<Duration>ofNullable(telemetryLevelInfo.getMaxAgeDuration()).orElse(cacheExpirationDuration);
          }
        }).refreshAfterWrite(cacheRefreshIntervalMillis, TimeUnit.MILLISECONDS);
    if (_log.isDebugEnabled()) {
      cacheBuilder.recordStats();
      cacheBuilder.removalListener((collectorAgent, telemetryLevelInfo, removalCause) -> _log.debug("({}) Removing cache entry for {}", removalCause, collectorAgent));
    } 
    return cacheBuilder.build(new SynchronousCacheLoader<CollectorAgent, TelemetryLevelInfo>() {
          public DefaultTelemetryLevelService.TelemetryLevelInfo load(CollectorAgent collectorAgent) throws Exception {
            return DefaultTelemetryLevelService.this.getTelemetryLevelInfo(collectorAgent);
          }
        });
  }
  
  @VisibleForTesting
  Ticker createCacheTicker() {
    return Ticker.systemTicker();
  }
  
  boolean isTelemetryLevelCached(String collectorId, String collectorInstanceId) {
    return this._telemetryLevelInfoCache.asMap().containsKey(new CollectorAgent(collectorId, collectorInstanceId));
  }
  
  private static class TelemetryLevelInfo {
    private final TelemetryLevel _telemetryLevel;
    
    private final Duration _maxAgeDuration;
    
    public TelemetryLevelInfo(TelemetryLevel telemetryLevel, Duration maxAgeDuration) {
      this._telemetryLevel = telemetryLevel;
      this._maxAgeDuration = maxAgeDuration;
    }
    
    public TelemetryLevel getTelemetryLevel() {
      return this._telemetryLevel;
    }
    
    public Duration getMaxAgeDuration() {
      return this._maxAgeDuration;
    }
  }
}
