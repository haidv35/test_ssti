package com.vmware.ph.phservice.cloud.dataapp.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfoId;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;
import com.vmware.ph.phservice.common.internal.cache.caffeine.ExpireAfterCreateExpiry;
import com.vmware.ph.phservice.common.manifest.MaxAgeParser;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManifestCachingDataAppWrapper extends DefaultDataApp {
  private static final Log _log = LogFactory.getLog(ManifestCachingDataAppWrapper.class);
  
  private static final long DEFAULT_CACHE_EXPIRATION_MILLIS = TimeUnit.HOURS
    .toMillis(2L);
  
  private static final int DEFAULT_CACHE_CAPACITY = 1024;
  
  private final DefaultDataApp _dataApp;
  
  private final Cache<ManifestInfoId, Optional<ManifestInfo>> _keyToManifestInfoCache;
  
  private final MaxAgeParser _maxAgeParser;
  
  public ManifestCachingDataAppWrapper(DefaultDataApp dataApp) {
    this(dataApp, DEFAULT_CACHE_EXPIRATION_MILLIS, 1024);
  }
  
  public ManifestCachingDataAppWrapper(DefaultDataApp dataApp, long cacheExpirationMillis, int cacheCapacity) {
    this._dataApp = dataApp;
    this._maxAgeParser = new MaxAgeParser();
    this

      
      ._keyToManifestInfoCache = Caffeine.newBuilder().maximumSize(cacheCapacity).expireAfter(createCacheExpiry(Duration.ofMillis(cacheExpirationMillis))).build();
  }
  
  public ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId) {
    ManifestInfoId manifestInfoId = new ManifestInfoId(collectorId, collectorInstanceId);
    return ((Optional<ManifestInfo>)this._keyToManifestInfoCache.get(manifestInfoId, key -> Optional.ofNullable(this._dataApp.getManifestInfo(collectorId, collectorInstanceId))))
      
      .orElse(null);
  }
  
  public ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, String manifestDataType, String manifestObjectId, String versionDataType, String versionObjectId) {
    ManifestInfoId manifestInfoId = new ManifestInfoId(collectorId, collectorInstanceId, manifestDataType, manifestObjectId, versionDataType, versionObjectId);
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Retrieving ManifestInfo with  ManifestInfoId: %s", new Object[] { manifestInfoId.toString() })); 
    return ((Optional<ManifestInfo>)this._keyToManifestInfoCache.get(manifestInfoId, key -> Optional.ofNullable(this._dataApp.getManifestInfo(collectorId, collectorInstanceId, manifestDataType, manifestObjectId, versionDataType, versionObjectId))))





      
      .orElse(null);
  }
  
  public ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, ManifestSpec manifestSpec, String objectId) {
    return super.getManifestInfo(collectorId, collectorInstanceId, manifestSpec, objectId);
  }
  
  public void uploadData(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, PluginData data) {
    this._dataApp.uploadData(collectorId, collectorInstanceId, collectionId, deploymentSecret, data);
  }
  
  public String getResult(String collectorId, String collectorInstanceId, String deploymentSecret, String dataType, String objectId, Long sinceTimestamp) {
    return this._dataApp.getResult(collectorId, collectorInstanceId, deploymentSecret, dataType, objectId, sinceTimestamp);
  }
  
  private Expiry<ManifestInfoId, Optional<ManifestInfo>> createCacheExpiry(final Duration defaultCacheDuration) {
    return new ExpireAfterCreateExpiry<ManifestInfoId, Optional<ManifestInfo>>() {
        protected Duration getDuration(ManifestInfoId key, Optional<ManifestInfo> manifestInfo) {
          return manifestInfo.map(ManifestInfo::getContent)
            .flatMap(ManifestCachingDataAppWrapper.this._maxAgeParser::parseDuration)
            .orElse(defaultCacheDuration);
        }
      };
  }
  
  public Map<ManifestInfoId, ManifestInfo> exportManifestCache() {
    return (Map<ManifestInfoId, ManifestInfo>)this._keyToManifestInfoCache
      .asMap()
      .entrySet()
      .stream()
      .filter(o -> ((Optional)o.getValue()).isPresent())
      .collect(Collectors.toMap(Map.Entry::getKey, o -> (ManifestInfo)((Optional<ManifestInfo>)o.getValue()).get()));
  }
}
