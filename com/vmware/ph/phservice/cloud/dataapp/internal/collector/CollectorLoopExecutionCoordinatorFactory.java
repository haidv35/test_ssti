package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.collector.internal.scheduler.CollectorLoopExecutionCoordinator;
import com.vmware.ph.phservice.collector.internal.scheduler.DefaultCollectorLoopExecutionCoordinator;
import com.vmware.ph.phservice.collector.scheduler.PersistenceServiceCollectorLoopExecutionTracker;
import com.vmware.ph.phservice.common.InMemoryPersistenceService;
import com.vmware.ph.phservice.common.internal.ConfigurationService;

public class CollectorLoopExecutionCoordinatorFactory {
  private final ConfigurationService _configurationService;
  
  private final String _defaultPropNameForForceCollectAlways;
  
  public CollectorLoopExecutionCoordinatorFactory() {
    this(null, null);
  }
  
  public CollectorLoopExecutionCoordinatorFactory(ConfigurationService configurationService, String defaultPropNameForForceCollectAlways) {
    this._configurationService = configurationService;
    this._defaultPropNameForForceCollectAlways = defaultPropNameForForceCollectAlways;
  }
  
  public CollectorLoopExecutionCoordinator create(DataAppAgentIdProvider agentIdProvider) {
    CollectorLoopExecutionCoordinator coordinator = new DefaultCollectorLoopExecutionCoordinator(new PersistenceServiceCollectorLoopExecutionTracker(new InMemoryPersistenceService()));
    if (this._configurationService != null && this._defaultPropNameForForceCollectAlways != null)
      coordinator = new PropertyControlledDataAppCollectorLoopExecutionCoordinatorWrapper(agentIdProvider, coordinator, this._configurationService, this._defaultPropNameForForceCollectAlways); 
    return coordinator;
  }
}
