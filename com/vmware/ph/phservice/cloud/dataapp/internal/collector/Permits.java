package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Permits {
  private final Semaphore _semaphore;
  
  private final long _timeoutMillis;
  
  public Permits(Semaphore semaphore, long timeoutMillis) {
    this._semaphore = semaphore;
    this._timeoutMillis = timeoutMillis;
  }
  
  class Permission implements AutoCloseable {
    private final boolean _granted;
    
    private Permission(boolean granted) {
      this._granted = granted;
    }
    
    public void close() {
      if (this._granted)
        Permits.this._semaphore.release(); 
    }
    
    boolean isGranted() {
      return this._granted;
    }
  }
  
  Permission get() {
    boolean acquired;
    try {
      acquired = this._semaphore.tryAcquire(this._timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      acquired = false;
      Thread.currentThread().interrupt();
    } 
    return new Permission(acquired);
  }
}
