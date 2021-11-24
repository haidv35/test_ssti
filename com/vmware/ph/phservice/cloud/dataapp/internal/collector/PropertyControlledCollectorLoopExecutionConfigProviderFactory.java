package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.collector.scheduler.CollectorLoopExecutionConfigProvider;
import com.vmware.ph.phservice.collector.scheduler.PropertyControlledLoopExecutionConfigProvider;
import com.vmware.ph.phservice.common.internal.ConfigurationService;

public class PropertyControlledCollectorLoopExecutionConfigProviderFactory {
  private final ConfigurationService _configurationService;
  
  private final String _propNameForInitBackOffTime;
  
  private final String _propNameForScheduleCheckTime;
  
  private final String _propNameForBackOffTimeOnError;
  
  private final String _propNameForCollectorThreadTimeout;
  
  private final String _propNameForRunCollectorOnce;
  
  private final long _defaultInitBackoffTimeSeconds;
  
  private final long _defaultScheduleCheckTimeSeconds;
  
  private final long _defaultBackoffTimeOnErrorSeconds;
  
  private final long _defaultCollectorThreadTimeoutSeconds;
  
  public PropertyControlledCollectorLoopExecutionConfigProviderFactory(ConfigurationService configurationService, String propNameForInitBackOffTime, String propNameForScheduleCheckTime, String propNameForBackOffTimeOnError, String propNameForCollectorThreadTimeout, String propNameForRunCollectorOnce, long defaultInitBackoffTimeSeconds, long defaultScheduleCheckTimeSeconds, long defaultBackoffTimeOnErrorSeconds, long defaultCollectorThreadTimeoutSeconds) {
    this._configurationService = configurationService;
    this._propNameForInitBackOffTime = propNameForInitBackOffTime;
    this._propNameForScheduleCheckTime = propNameForScheduleCheckTime;
    this._propNameForBackOffTimeOnError = propNameForBackOffTimeOnError;
    this._propNameForCollectorThreadTimeout = propNameForCollectorThreadTimeout;
    this._propNameForRunCollectorOnce = propNameForRunCollectorOnce;
    this._defaultInitBackoffTimeSeconds = defaultInitBackoffTimeSeconds;
    this._defaultScheduleCheckTimeSeconds = defaultScheduleCheckTimeSeconds;
    this._defaultBackoffTimeOnErrorSeconds = defaultBackoffTimeOnErrorSeconds;
    this._defaultCollectorThreadTimeoutSeconds = defaultCollectorThreadTimeoutSeconds;
  }
  
  public CollectorLoopExecutionConfigProvider create(DataAppAgentIdProvider agentIdProvider) {
    PropertyControlledLoopExecutionConfigProvider generalConfigProvider = new PropertyControlledLoopExecutionConfigProvider(this._configurationService, this._propNameForInitBackOffTime, this._propNameForScheduleCheckTime, this._propNameForBackOffTimeOnError, this._propNameForCollectorThreadTimeout, null, this._propNameForRunCollectorOnce, this._defaultInitBackoffTimeSeconds, this._defaultScheduleCheckTimeSeconds, this._defaultBackoffTimeOnErrorSeconds, this._defaultCollectorThreadTimeoutSeconds, 0L);
    if (agentIdProvider == null)
      return generalConfigProvider; 
    return new AgentSpecificLoopExecutionConfigProvider(this._configurationService, agentIdProvider, generalConfigProvider, this._propNameForInitBackOffTime, this._propNameForScheduleCheckTime, this._propNameForBackOffTimeOnError, this._propNameForCollectorThreadTimeout, this._propNameForRunCollectorOnce);
  }
}
