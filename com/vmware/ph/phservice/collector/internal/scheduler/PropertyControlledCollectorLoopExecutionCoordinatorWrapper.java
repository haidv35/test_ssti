package com.vmware.ph.phservice.collector.internal.scheduler;

import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyControlledCollectorLoopExecutionCoordinatorWrapper implements CollectorLoopExecutionCoordinator {
  private static final Log _log = LogFactory.getLog(PropertyControlledCollectorLoopExecutionCoordinatorWrapper.class);
  
  private final CollectorLoopExecutionCoordinator _wrappedCollectorLoopExecutionCoordinator;
  
  private final ConfigurationService _configurationService;
  
  private final String _propNameForForceCollectAlways;
  
  public PropertyControlledCollectorLoopExecutionCoordinatorWrapper(CollectorLoopExecutionCoordinator collectorLoopExecutionCoordinator, ConfigurationService configurationService, String propNameForForceCollectAlways) {
    this._wrappedCollectorLoopExecutionCoordinator = collectorLoopExecutionCoordinator;
    this._configurationService = configurationService;
    this._propNameForForceCollectAlways = propNameForForceCollectAlways;
  }
  
  public void recordFirstBootTime(long firstBootTime) {
    this._wrappedCollectorLoopExecutionCoordinator.recordFirstBootTime(firstBootTime);
  }
  
  public boolean isItTimeToCollect(CollectionSchedule schedule) {
    boolean isItTimeToCollect = this._wrappedCollectorLoopExecutionCoordinator.isItTimeToCollect(schedule);
    Boolean rawValue = getBoolConfigProperty(this._propNameForForceCollectAlways);
    boolean forcedCollection = (rawValue == null) ? false : rawValue.booleanValue();
    if (forcedCollection && _log.isInfoEnabled())
      _log.info("Collection will happen, becasue it is forced via configuration."); 
    return (forcedCollection || isItTimeToCollect);
  }
  
  public void recordCollectionOutcome(CollectionSchedule schedule, long collectionStartTimeMillis, CollectorOutcome outcome) {
    this._wrappedCollectorLoopExecutionCoordinator
      .recordCollectionOutcome(schedule, collectionStartTimeMillis, outcome);
  }
  
  public void updateCollectionStats(Set<CollectionSchedule> schedules) {
    this._wrappedCollectorLoopExecutionCoordinator.updateCollectionStats(schedules);
  }
  
  protected Boolean getBoolConfigProperty(String propName) {
    return this._configurationService.getBoolProperty(propName);
  }
}
