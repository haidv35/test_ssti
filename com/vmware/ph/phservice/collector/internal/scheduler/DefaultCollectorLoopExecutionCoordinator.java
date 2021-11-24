package com.vmware.ph.phservice.collector.internal.scheduler;

import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.collector.scheduler.CollectionScheduleExecutionState;
import com.vmware.ph.phservice.collector.scheduler.CollectorLoopExecutionTracker;
import java.util.Calendar;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultCollectorLoopExecutionCoordinator implements CollectorLoopExecutionCoordinator {
  private static final int COLLECTION_NO_RETRY_FAILURES_COUNT = 0;
  
  private static final Log _log = LogFactory.getLog(DefaultCollectorLoopExecutionCoordinator.class);
  
  private final CollectorLoopExecutionTracker _collectorLoopExecutionTracker;
  
  private final ScheduleCalculator _scheduleCalculator;
  
  public DefaultCollectorLoopExecutionCoordinator(CollectorLoopExecutionTracker collectorLoopExecutionTracker) {
    this._collectorLoopExecutionTracker = collectorLoopExecutionTracker;
    this._scheduleCalculator = new ScheduleCalculator();
  }
  
  public DefaultCollectorLoopExecutionCoordinator(CollectorLoopExecutionTracker collectorLoopExecutionTracker, ScheduleCalculator scheduleCalculator) {
    this._collectorLoopExecutionTracker = collectorLoopExecutionTracker;
    this._scheduleCalculator = scheduleCalculator;
  }
  
  public void recordFirstBootTime(long firstBootTime) {
    try {
      if (this._collectorLoopExecutionTracker.getFirstBootTime() == 0L)
        this._collectorLoopExecutionTracker.setFirstBootTime(firstBootTime); 
    } catch (Exception e) {
      if (_log.isErrorEnabled())
        _log.error("Error reading/writing firstboot time during startup.", e); 
    } 
  }
  
  public boolean isItTimeToCollect(CollectionSchedule schedule) {
    CollectionScheduleExecutionState lastScheduleExecutionState = this._collectorLoopExecutionTracker.getCollectionScheduleExecutionState(schedule);
    lastScheduleExecutionState = fixCollectionSchedulePatternIfNeeded(lastScheduleExecutionState);
    long collectionFirstBootTime = this._collectorLoopExecutionTracker.getFirstBootTime();
    long currentTimeMillis = System.currentTimeMillis();
    boolean isItTimeToCollect = this._scheduleCalculator.isItTimeToRunScheduledCollection(lastScheduleExecutionState, collectionFirstBootTime, currentTimeMillis);
    if (!isItTimeToCollect)
      isItTimeToCollect = this._scheduleCalculator.isItTimeToRetryCollection(lastScheduleExecutionState, currentTimeMillis); 
    return isItTimeToCollect;
  }
  
  public void recordCollectionOutcome(CollectionSchedule schedule, long collectionStartTimeMillis, CollectorOutcome outcome) {
    CollectionScheduleExecutionState lastScheduleExecutionState = this._collectorLoopExecutionTracker.getCollectionScheduleExecutionState(schedule);
    lastScheduleExecutionState = fixCollectionSchedulePatternIfNeeded(lastScheduleExecutionState);
    long collectionFirstBootTime = this._collectorLoopExecutionTracker.getFirstBootTime();
    CollectionScheduleExecutionState newCollectionScheduleExecutionState = calculateNewExecutionState(lastScheduleExecutionState, collectionFirstBootTime, collectionStartTimeMillis, outcome, this._scheduleCalculator);
    this._collectorLoopExecutionTracker.setCollectionScheduleExecutionState(newCollectionScheduleExecutionState);
  }
  
  public void updateCollectionStats(Set<CollectionSchedule> schedules) {
    this._collectorLoopExecutionTracker.removeCollectionScheduleExecutionStates(schedules);
  }
  
  private CollectionScheduleExecutionState fixCollectionSchedulePatternIfNeeded(CollectionScheduleExecutionState executionState) {
    CollectionSchedule schedule = executionState.getSchedule();
    if (schedule.getInterval() >= 3600000L) {
      String schedulePattern = executionState.getSchedulePattern();
      if (!isSchedulePatternValid(schedulePattern)) {
        CollectionScheduleExecutionState scheduleExecutionState = calculateNewExecutionStateWithNewPattern(executionState, this._scheduleCalculator);
        this._collectorLoopExecutionTracker.setCollectionScheduleExecutionState(scheduleExecutionState);
        executionState = scheduleExecutionState;
      } 
    } 
    return executionState;
  }
  
  private boolean isSchedulePatternValid(String schedulePattern) {
    if (StringUtils.isEmpty(schedulePattern))
      return false; 
    Calendar scheduleCalendar = this._scheduleCalculator.validateScheduleAndBuildCalendar(schedulePattern, false);
    return (scheduleCalendar != null);
  }
  
  private static CollectionScheduleExecutionState calculateNewExecutionStateWithNewPattern(CollectionScheduleExecutionState scheduleExecutionState, ScheduleCalculator scheduleCalculator) {
    String validSchedulePattern = scheduleCalculator.generateRandomSchedulePattern();
    scheduleExecutionState = calculateNewExecutionState(scheduleExecutionState, validSchedulePattern);
    return scheduleExecutionState;
  }
  
  private static CollectionScheduleExecutionState calculateNewExecutionState(CollectionScheduleExecutionState lastScheduleExecutionState, long collectionFirstBootTime, long collectionStartTimeMillis, CollectorOutcome collectionOutcome, ScheduleCalculator scheduleCalculator) {
    boolean isScheduledCollection = scheduleCalculator.isItTimeToRunScheduledCollection(lastScheduleExecutionState, collectionFirstBootTime, collectionStartTimeMillis);
    boolean isCollectionSuccessful = (collectionOutcome == CollectorOutcome.PASSED);
    return calculateNewExecutionState(lastScheduleExecutionState, isScheduledCollection, isCollectionSuccessful, collectionStartTimeMillis);
  }
  
  private static CollectionScheduleExecutionState calculateNewExecutionState(CollectionScheduleExecutionState previousExecutionState, boolean isScheduledCollection, boolean isCollectionSuccessful, long newCollectionStartTimeMillis) {
    long newCollectionCompleteTime = (isScheduledCollection || isCollectionSuccessful) ? System.currentTimeMillis() : previousExecutionState.getLastCollectionCompleteTime();
    int collectionFailureCount = calculateFailureCountBasedOnCollectionOutcome(previousExecutionState, isScheduledCollection, isCollectionSuccessful);
    return new CollectionScheduleExecutionState(previousExecutionState
        .getSchedule(), previousExecutionState
        .getSchedulePattern(), 
        Long.valueOf(newCollectionStartTimeMillis), 
        Long.valueOf(newCollectionCompleteTime), 
        Integer.valueOf(collectionFailureCount));
  }
  
  private static CollectionScheduleExecutionState calculateNewExecutionState(CollectionScheduleExecutionState executionState, String newSchedulePattern) {
    CollectionScheduleExecutionState updatedExecutionState = new CollectionScheduleExecutionState(executionState.getSchedule(), newSchedulePattern, Long.valueOf(executionState.getLastCollectionStartTime()), Long.valueOf(executionState.getLastCollectionCompleteTime()), Integer.valueOf(executionState.getFailureCount()));
    if (_log.isInfoEnabled())
      _log.info(
          String.format("The data collection was enabled but the configured schedule pattern '%s' for schedule '%s' was not valid. Created new schedule pattern: %s", new Object[] { executionState.getSchedulePattern(), executionState
              .getSchedule(), newSchedulePattern })); 
    return updatedExecutionState;
  }
  
  private static int calculateFailureCountBasedOnCollectionOutcome(CollectionScheduleExecutionState originalExecutionState, boolean isScheduledCollection, boolean isCollectionSuccessful) {
    int collectionFailureCount = (isScheduledCollection || isCollectionSuccessful) ? 0 : originalExecutionState.getFailureCount();
    CollectionSchedule schedule = originalExecutionState.getSchedule();
    if (!isCollectionSuccessful && schedule
      .getShouldRetryOnFailure() && collectionFailureCount <= schedule
      .getMaxRetriesCount())
      collectionFailureCount++; 
    return collectionFailureCount;
  }
}
