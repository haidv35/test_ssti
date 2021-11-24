package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.util.AgentPropertyNameUtil;
import com.vmware.ph.phservice.collector.internal.scheduler.CollectorLoopExecutionCoordinator;
import com.vmware.ph.phservice.collector.internal.scheduler.PropertyControlledCollectorLoopExecutionCoordinatorWrapper;
import com.vmware.ph.phservice.common.internal.ConfigurationService;

public class PropertyControlledDataAppCollectorLoopExecutionCoordinatorWrapper extends PropertyControlledCollectorLoopExecutionCoordinatorWrapper {
  private DataAppAgentIdProvider _agentIdProvider;
  
  public PropertyControlledDataAppCollectorLoopExecutionCoordinatorWrapper(DataAppAgentIdProvider agentIdProvider, CollectorLoopExecutionCoordinator collectorLoopExecutionCoordinator, ConfigurationService configurationService, String defaultPropNameForForceCollectAlways) {
    super(collectorLoopExecutionCoordinator, configurationService, defaultPropNameForForceCollectAlways);
    this._agentIdProvider = agentIdProvider;
  }
  
  protected Boolean getBoolConfigProperty(String defaultPropNameForForceCollectAlways) {
    String agentPropNameForForceCollectAlways = AgentPropertyNameUtil.getAgentSpecificPropertyName(this._agentIdProvider, defaultPropNameForForceCollectAlways);
    Boolean rawValue = super.getBoolConfigProperty(agentPropNameForForceCollectAlways);
    if (rawValue == null)
      rawValue = super.getBoolConfigProperty(defaultPropNameForForceCollectAlways); 
    return rawValue;
  }
}
