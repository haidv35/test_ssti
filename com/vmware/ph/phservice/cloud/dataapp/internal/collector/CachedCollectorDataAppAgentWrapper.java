package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CachedCollectorDataAppAgentWrapper extends BaseCollectorDataAppAgentWrapper {
  private volatile Cache<String, Optional<PluginResult>> _agentResultsCache;
  
  private final int _cacheCapacity;
  
  public CachedCollectorDataAppAgentWrapper(CollectorDataAppAgent wrappedAgent, int cacheCapacity) {
    super(wrappedAgent);
    this._cacheCapacity = cacheCapacity;
    this._agentResultsCache = newCache();
  }
  
  public PluginResult execute(String objectId, Object contextData, boolean useCache) {
    PluginResult pluginResult;
    if (useCache) {
      Optional<PluginResult> optionalPluginResult = (Optional<PluginResult>)this._agentResultsCache.getIfPresent(objectId);
      pluginResult = (optionalPluginResult != null) ? optionalPluginResult.orElse(null) : null;
    } else {
      pluginResult = super.execute(objectId, contextData, useCache);
      refreshCacheForObjectId(objectId, pluginResult);
    } 
    return pluginResult;
  }
  
  public PluginResult execute(String objectId, Object contextData, ProgressReporter progressReporter) {
    PluginResult pluginResult = super.execute(objectId, contextData, progressReporter);
    refreshCacheForObjectId(objectId, pluginResult);
    return pluginResult;
  }
  
  public Iterable<PluginResult> execute(Map<String, Object> objectIdToContextData, AgentResultRetriever resultRetriever, CollectionSchedule schedule) {
    Iterable<PluginResult> results = super.execute(objectIdToContextData, resultRetriever, schedule);
    refreshCacheForPluginResults(results);
    return results;
  }
  
  public AgentStatus getAgentStatus() {
    AgentStatus agentStatus = this._wrappedAgent.getAgentStatus();
    agentStatus.setCachedResults((List<PluginResult>)this._agentResultsCache
        .asMap().values()
        .stream()
        .map(result -> (PluginResult)result.orElse(null))
        .collect(Collectors.toList()));
    return agentStatus;
  }
  
  private void refreshCacheForObjectId(String objectId, PluginResult pluginResult) {
    if (objectId != null) {
      this._agentResultsCache.put(objectId, Optional.ofNullable(pluginResult));
    } else if (pluginResult != null && pluginResult.getObjectId() != null) {
      this._agentResultsCache.put(pluginResult.getObjectId(), Optional.of(pluginResult));
    } 
  }
  
  private void refreshCacheForPluginResults(Iterable<PluginResult> pluginResults) {
    if (pluginResults == null)
      return; 
    Cache<String, Optional<PluginResult>> newCache = newCache();
    StreamSupport.stream(pluginResults.spliterator(), false)
      .filter(result -> (result != null && result.getObjectId() != null))
      .forEach(result -> newCache.put(result.getObjectId(), Optional.of(result)));
    this._agentResultsCache = newCache;
  }
  
  @VisibleForTesting
  Cache<String, Optional<PluginResult>> getAgentResultsCache() {
    return this._agentResultsCache;
  }
  
  private Cache<String, Optional<PluginResult>> newCache() {
    return CacheBuilder.newBuilder().maximumSize(this._cacheCapacity).build();
  }
}
