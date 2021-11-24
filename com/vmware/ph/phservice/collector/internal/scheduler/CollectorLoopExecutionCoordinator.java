package com.vmware.ph.phservice.collector.internal.scheduler;

import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Set;

public interface CollectorLoopExecutionCoordinator {
  void recordFirstBootTime(long paramLong);
  
  boolean isItTimeToCollect(CollectionSchedule paramCollectionSchedule);
  
  void recordCollectionOutcome(CollectionSchedule paramCollectionSchedule, long paramLong, CollectorOutcome paramCollectorOutcome);
  
  void updateCollectionStats(Set<CollectionSchedule> paramSet);
}
