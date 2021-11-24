package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.util.AgentPropertyNameUtil;
import com.vmware.ph.phservice.collector.scheduler.CollectorLoopExecutionConfigProvider;
import com.vmware.ph.phservice.collector.scheduler.PropertyControlledLoopExecutionConfigProvider;
import com.vmware.ph.phservice.common.internal.ConfigurationService;

public class AgentSpecificLoopExecutionConfigProvider implements CollectorLoopExecutionConfigProvider {
  private final ConfigurationService _configurationService;
  
  private final DataAppAgentIdProvider _agentIdProvider;
  
  private final PropertyControlledLoopExecutionConfigProvider _defaultConfigProvider;
  
  private final String _propNameForInitBackOffTime;
  
  private final String _propNameForScheduleCheckTime;
  
  private final String _propNameForBackOffTimeOnError;
  
  private final String _propNameForCollectorThreadTimeout;
  
  private final String _propNameForRunCollectorOnce;
  
  public AgentSpecificLoopExecutionConfigProvider(ConfigurationService configurationService, DataAppAgentIdProvider agentIdProvider, PropertyControlledLoopExecutionConfigProvider defaultConfigProvider, String propNameForInitBackOffTime, String propNameForScheduleCheckTime, String propNameForBackOffTimeOnError, String propNameForCollectorThreadTimeout, String propNameForRunCollectorOnce) {
    this._configurationService = configurationService;
    this._agentIdProvider = agentIdProvider;
    this._defaultConfigProvider = defaultConfigProvider;
    this._propNameForInitBackOffTime = propNameForInitBackOffTime;
    this._propNameForScheduleCheckTime = propNameForScheduleCheckTime;
    this._propNameForBackOffTimeOnError = propNameForBackOffTimeOnError;
    this._propNameForCollectorThreadTimeout = propNameForCollectorThreadTimeout;
    this._propNameForRunCollectorOnce = propNameForRunCollectorOnce;
  }
  
  public long getInitialBackOffTimeSeconds() {
    String agentSpecificPropertyName = AgentPropertyNameUtil.getAgentSpecificPropertyName(this._agentIdProvider, this._propNameForInitBackOffTime);
    Long agentSpecificPropertyValue = this._configurationService.getLongProperty(agentSpecificPropertyName);
    if (agentSpecificPropertyValue != null)
      return agentSpecificPropertyValue.longValue(); 
    return this._defaultConfigProvider.getInitialBackOffTimeSeconds();
  }
  
  public long getBackoffTimeSeconds() {
    String agentSpecificPropertyName = AgentPropertyNameUtil.getAgentSpecificPropertyName(this._agentIdProvider, this._propNameForBackOffTimeOnError);
    Long agentSpecificPropertyValue = this._configurationService.getLongProperty(agentSpecificPropertyName);
    if (agentSpecificPropertyValue != null)
      return agentSpecificPropertyValue.longValue(); 
    return this._defaultConfigProvider.getBackoffTimeSeconds();
  }
  
  public long getScheduleCheckTimeSeconds() {
    String agentSpecificPropertyName = AgentPropertyNameUtil.getAgentSpecificPropertyName(this._agentIdProvider, this._propNameForScheduleCheckTime);
    Long agentSpecificPropertyValue = this._configurationService.getLongProperty(agentSpecificPropertyName);
    if (agentSpecificPropertyValue != null)
      return agentSpecificPropertyValue.longValue(); 
    return this._defaultConfigProvider.getScheduleCheckTimeSeconds();
  }
  
  public boolean shouldRunOnce() {
    String agentSpecificPropertyName = AgentPropertyNameUtil.getAgentSpecificPropertyName(this._agentIdProvider, this._propNameForRunCollectorOnce);
    Boolean agentSpecificPropertyValue = this._configurationService.getBoolProperty(agentSpecificPropertyName);
    if (agentSpecificPropertyValue != null)
      return agentSpecificPropertyValue.booleanValue(); 
    return this._defaultConfigProvider.shouldRunOnce();
  }
  
  public long getCollectorThreadTimeoutSeconds() {
    String agentSpecificPropertyName = AgentPropertyNameUtil.getAgentSpecificPropertyName(this._agentIdProvider, this._propNameForCollectorThreadTimeout);
    Long agentSpecificPropertyValue = this._configurationService.getLongProperty(agentSpecificPropertyName);
    if (agentSpecificPropertyValue != null)
      return agentSpecificPropertyValue.longValue(); 
    return this._defaultConfigProvider.getCollectorThreadTimeoutSeconds();
  }
}
