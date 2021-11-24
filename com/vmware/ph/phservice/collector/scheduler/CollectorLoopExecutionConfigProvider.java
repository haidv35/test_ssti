package com.vmware.ph.phservice.collector.scheduler;

public interface CollectorLoopExecutionConfigProvider {
  public static final long NO_TIMEOUT = 0L;
  
  long getInitialBackOffTimeSeconds();
  
  long getBackoffTimeSeconds();
  
  long getScheduleCheckTimeSeconds();
  
  boolean shouldRunOnce();
  
  long getCollectorThreadTimeoutSeconds();
  
  default long getCollectorLoopTimeoutSeconds() {
    return 0L;
  }
}
