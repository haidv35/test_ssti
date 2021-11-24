package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PermitControlledCollectorWrapper implements Collector {
  private static final Log _log = LogFactory.getLog(PermitControlledCollectorWrapper.class);
  
  private final Semaphore _collectorSemaphore;
  
  private final Collector _wrappedCollector;
  
  private final long _timeoutInMillis;
  
  public PermitControlledCollectorWrapper(Collector wrappedCollector, Semaphore collectorSemaphore, long timeoutInMillis) {
    this._wrappedCollector = Objects.<Collector>requireNonNull(wrappedCollector);
    this._collectorSemaphore = Objects.<Semaphore>requireNonNull(collectorSemaphore);
    this._timeoutInMillis = timeoutInMillis;
  }
  
  public void setContextData(Object contextData) {
    this._wrappedCollector.setContextData(contextData);
  }
  
  public void run() {
    CollectorOutcome collectorOutcome = collect();
    if (collectorOutcome != CollectorOutcome.PASSED)
      throw new RuntimeException("Usage data collection failed!"); 
  }
  
  public CollectorOutcome collect() {
    CollectorOutcome outcome;
    boolean hasAcquired = false;
    try {
      hasAcquired = this._collectorSemaphore.tryAcquire(this._timeoutInMillis, TimeUnit.MILLISECONDS);
      if (hasAcquired) {
        outcome = this._wrappedCollector.collect();
      } else {
        _log.warn("Could not obtain a permit to run the collector. Skipping the collection.");
        outcome = CollectorOutcome.LOCAL_ERROR;
      } 
    } catch (InterruptedException e) {
      _log.warn("Collector was interrupted while waiting to obtain a permit. Skipping the collection.");
      Thread.currentThread().interrupt();
      outcome = CollectorOutcome.LOCAL_ERROR;
    } finally {
      if (hasAcquired)
        this._collectorSemaphore.release(); 
    } 
    return outcome;
  }
  
  public void close() {
    this._wrappedCollector.close();
  }
}
