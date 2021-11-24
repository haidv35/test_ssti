package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PermitControlledCollectorDataAppAgentWrapper extends BaseCollectorDataAppAgentWrapper {
  private static final Log _log = LogFactory.getLog(PermitControlledCollectorDataAppAgentWrapper.class);
  
  private final Permits _permits;
  
  public PermitControlledCollectorDataAppAgentWrapper(CollectorDataAppAgent wrappedAgent, Permits permits) {
    super(wrappedAgent);
    this._permits = permits;
  }
  
  public PluginResult execute(String objectId, Object contextData, boolean useCache) {
    return executeWithPermit(() -> access$301(this, objectId, contextData, useCache));
  }
  
  public PluginResult execute(String objectId, Object contextData, ProgressReporter progressReporter) {
    return executeWithPermit(() -> access$201(this, objectId, contextData, progressReporter));
  }
  
  public Iterable<PluginResult> execute(Map<String, Object> objectIdToContextData, AgentResultRetriever agentResultRetriever, CollectionSchedule schedule) {
    return executeWithPermit(() -> access$101(this, objectIdToContextData, agentResultRetriever, schedule));
  }
  
  public String collect(String manifestContent, String objectId, Object contextData) {
    return executeWithPermit(() -> access$001(this, manifestContent, objectId, contextData));
  }
  
  private <T> T executeWithPermit(Callable<T> func) {
    T result = null;
    try (Permits.Permission permission = this._permits.get()) {
      if (permission.isGranted()) {
        result = func.call();
      } else {
        _log.warn("Could not obtain a permit to execute the data app. Skipping the execution.");
      } 
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        _log.debug("There was an error while waiting to obtain a permit. Skipping the execution.", e);
      } else if (_log.isWarnEnabled()) {
        _log.warn("There was an error while waiting to obtain a permit. Skipping the execution.");
      } 
    } 
    return result;
  }
}
