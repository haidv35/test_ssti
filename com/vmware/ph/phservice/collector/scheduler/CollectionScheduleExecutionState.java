package com.vmware.ph.phservice.collector.scheduler;

public class CollectionScheduleExecutionState {
  private final CollectionSchedule _schedule;
  
  private final String _schedulePattern;
  
  private final Long _lastCollectionStartTime;
  
  private final Long _lastCollectionCompleteTime;
  
  private final Integer _failureCount;
  
  public CollectionScheduleExecutionState(CollectionSchedule schedule, String schedulePattern, Long lastCollectionStartTime, Long lastCollectionCompleteTime, Integer failureCount) {
    this._schedule = schedule;
    this._schedulePattern = schedulePattern;
    this._lastCollectionStartTime = lastCollectionStartTime;
    this._lastCollectionCompleteTime = lastCollectionCompleteTime;
    this._failureCount = failureCount;
  }
  
  public CollectionSchedule getSchedule() {
    return this._schedule;
  }
  
  public String getSchedulePattern() {
    return this._schedulePattern;
  }
  
  public long getLastCollectionStartTime() {
    if (this._lastCollectionStartTime == null)
      return 0L; 
    return this._lastCollectionStartTime.longValue();
  }
  
  public long getLastCollectionCompleteTime() {
    if (this._lastCollectionCompleteTime == null)
      return 0L; 
    return this._lastCollectionCompleteTime.longValue();
  }
  
  public int getFailureCount() {
    if (this._failureCount == null)
      return 0; 
    return this._failureCount.intValue();
  }
}
