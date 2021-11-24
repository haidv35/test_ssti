package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.phservice.collector.CollectorOutcome;

public interface Collector extends Runnable, AutoCloseable {
  void setContextData(Object paramObject);
  
  @Deprecated
  void run();
  
  CollectorOutcome collect();
  
  void close();
}
