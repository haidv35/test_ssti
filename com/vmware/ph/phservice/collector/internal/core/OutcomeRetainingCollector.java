package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;

public class OutcomeRetainingCollector implements Collector {
  private final Collector _collector;
  
  private volatile CollectorOutcome _collectorOutcome = null;
  
  public OutcomeRetainingCollector(Collector collector) {
    this._collector = collector;
  }
  
  public void setContextData(Object contextData) {
    this._collector.setContextData(contextData);
  }
  
  public void run() {
    try {
      this._collector.run();
      this._collectorOutcome = CollectorOutcome.PASSED;
    } catch (Exception e) {
      this._collectorOutcome = CollectorOutcome.LOCAL_ERROR;
      throw e;
    } 
  }
  
  public CollectorOutcome collect() {
    this._collectorOutcome = this._collector.collect();
    return this._collectorOutcome;
  }
  
  public void close() {
    this._collector.close();
  }
  
  public CollectorOutcome getCollectorOutcome() {
    return this._collectorOutcome;
  }
}
