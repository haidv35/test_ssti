package com.vmware.ph.phservice.collector.scheduler;

import java.util.Set;

public interface CollectorLoopExecutionTracker {
  long getFirstBootTime();
  
  void setFirstBootTime(long paramLong);
  
  CollectionScheduleExecutionState getCollectionScheduleExecutionState(CollectionSchedule paramCollectionSchedule);
  
  void setCollectionScheduleExecutionState(CollectionScheduleExecutionState paramCollectionScheduleExecutionState);
  
  void removeCollectionScheduleExecutionStates(Set<CollectionSchedule> paramSet);
}
