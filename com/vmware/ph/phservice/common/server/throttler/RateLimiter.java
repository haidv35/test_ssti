package com.vmware.ph.phservice.common.server.throttler;

import java.util.concurrent.TimeUnit;

public class RateLimiter {
  private final com.google.common.util.concurrent.RateLimiter _innerRateLimiter;
  
  public RateLimiter(double permitsPerSecond) {
    this
      ._innerRateLimiter = com.google.common.util.concurrent.RateLimiter.create(permitsPerSecond);
  }
  
  public boolean tryAcquire(long timeoutInMillis) {
    return this._innerRateLimiter.tryAcquire(timeoutInMillis, TimeUnit.MILLISECONDS);
  }
}
