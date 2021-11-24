package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Set;

public interface CollectorProvider {
  boolean isActive();
  
  Collector getCollector(CollectionSchedule paramCollectionSchedule);
  
  Set<CollectionSchedule> getCollectorSchedules();
}
