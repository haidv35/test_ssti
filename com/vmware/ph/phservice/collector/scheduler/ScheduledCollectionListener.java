package com.vmware.ph.phservice.collector.scheduler;

import com.vmware.ph.phservice.collector.CollectorOutcome;

public interface ScheduledCollectionListener {
  void handle(CollectorOutcome paramCollectorOutcome);
}
