package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class BaseCollectorDataAppAgentWrapper implements AsyncCollectorDataAppAgent {
  protected final CollectorDataAppAgent _wrappedAgent;
  
  public BaseCollectorDataAppAgentWrapper(CollectorDataAppAgent wrappedAgent) {
    this._wrappedAgent = wrappedAgent;
  }
  
  public CollectorDataAppAgent getWrappedAgent() {
    return this._wrappedAgent;
  }
  
  public DataAppAgentId getAgentId() {
    return this._wrappedAgent.getAgentId();
  }
  
  public PluginResult execute(String objectId, Object contextData, boolean useCache) {
    return this._wrappedAgent.execute(objectId, contextData, useCache);
  }
  
  public PluginResult execute(String objectId, Object contextData, ProgressReporter progressReporter) {
    return this._wrappedAgent.execute(objectId, contextData, progressReporter);
  }
  
  public Iterable<PluginResult> execute(Map<String, Object> objectIdToContextData, AgentResultRetriever resultRetriever, CollectionSchedule schedule) {
    return this._wrappedAgent.execute(objectIdToContextData, resultRetriever, schedule);
  }
  
  public String collect(String manifestContent, String objectId, Object contextData) {
    return this._wrappedAgent.collect(manifestContent, objectId, contextData);
  }
  
  public AgentCollectionScheduleSpec getCollectionScheduleSpec(Set<String> objectIds) {
    return this._wrappedAgent.getCollectionScheduleSpec(objectIds);
  }
  
  public AgentStatus getAgentStatus() {
    return this._wrappedAgent.getAgentStatus();
  }
  
  public Map<String, String> exportObfuscationMap(String objectId) {
    return this._wrappedAgent.exportObfuscationMap(objectId);
  }
  
  public void close() {
    this._wrappedAgent.close();
  }
  
  public Future<String> collectAsync(String manifestContent, String objectId, Object contextData) {
    if (this._wrappedAgent instanceof AsyncCollectorDataAppAgent)
      return ((AsyncCollectorDataAppAgent)this._wrappedAgent).collectAsync(manifestContent, objectId, contextData); 
    return CompletableFuture.completedFuture(this._wrappedAgent
        .collect(manifestContent, objectId, contextData));
  }
}
