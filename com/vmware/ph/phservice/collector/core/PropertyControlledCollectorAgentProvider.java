package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import org.apache.commons.lang.StringUtils;

public class PropertyControlledCollectorAgentProvider implements CollectorAgentProvider {
  private final ConfigurationService _configurationService;
  
  private final String _propNameForCollectorId;
  
  private final String _propNameForCollectorInstanceId;
  
  private final String _collectorInstanceIdPrefix;
  
  private final Builder<String> _instanceIdBuilder;
  
  public PropertyControlledCollectorAgentProvider(ConfigurationService configurationService, String propNameForCollectorId, String propNameForCollectorInstanceId) {
    this(configurationService, propNameForCollectorId, propNameForCollectorInstanceId, null, null);
  }
  
  public PropertyControlledCollectorAgentProvider(ConfigurationService configurationService, String propNameForCollectorId, String propNameForCollectorInstanceId, String collectorInstanceIdPrefix, Builder<String> instanceIdBuilder) {
    this._configurationService = configurationService;
    this._propNameForCollectorId = propNameForCollectorId;
    this._propNameForCollectorInstanceId = propNameForCollectorInstanceId;
    this._collectorInstanceIdPrefix = collectorInstanceIdPrefix;
    this._instanceIdBuilder = instanceIdBuilder;
  }
  
  public CollectorAgentProvider.CollectorAgentId getCollectorAgentId() {
    String collectorId = this._configurationService.getProperty(this._propNameForCollectorId);
    String collectorInstanceId = this._configurationService.getProperty(this._propNameForCollectorInstanceId);
    if (StringUtils.isBlank(collectorInstanceId) && this._instanceIdBuilder != null) {
      String collectorInstanceIdPrefix = this._collectorInstanceIdPrefix;
      if (StringUtils.isBlank(collectorInstanceIdPrefix))
        collectorInstanceIdPrefix = ""; 
      collectorInstanceId = (String)this._instanceIdBuilder.build();
      collectorInstanceId = collectorInstanceIdPrefix + collectorInstanceId;
    } 
    if (StringUtils.isBlank(collectorInstanceId))
      return null; 
    return new CollectorAgentProvider.CollectorAgentId(collectorId, collectorInstanceId);
  }
}
