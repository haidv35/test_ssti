package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import java.util.Map;

public class CeipCollectorDataAppAgentWrapper extends BaseCollectorDataAppAgentWrapper {
  private final CeipConfigProvider _ceipConfigProvider;
  
  public CeipCollectorDataAppAgentWrapper(CollectorDataAppAgent collectorDataAppAgent1, CeipConfigProvider ceipConfigProvider) {
    super(collectorDataAppAgent1);
    this._ceipConfigProvider = ceipConfigProvider;
  }
  
  public PluginResult execute(String objectId, Object contextData, boolean useCache) {
    if (!this._ceipConfigProvider.isCeipEnabled())
      return null; 
    return super.execute(objectId, contextData, useCache);
  }
  
  public PluginResult execute(String objectId, Object contextData, ProgressReporter progressReporter) {
    if (!this._ceipConfigProvider.isCeipEnabled())
      return null; 
    return super.execute(objectId, contextData, progressReporter);
  }
  
  public Iterable<PluginResult> execute(Map<String, Object> objectIdToContextData, AgentResultRetriever resultRetriever, CollectionSchedule schedule) {
    if (!this._ceipConfigProvider.isCeipEnabled())
      return null; 
    return super.execute(objectIdToContextData, resultRetriever, schedule);
  }
}
