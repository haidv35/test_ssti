package com.vmware.ph.phservice.collector.scheduler;

public interface CollectorLoop {
  void start();
  
  void stop() throws InterruptedException;
  
  boolean isRunning();
}
