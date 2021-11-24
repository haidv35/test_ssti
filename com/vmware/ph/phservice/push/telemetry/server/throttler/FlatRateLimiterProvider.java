package com.vmware.ph.phservice.push.telemetry.server.throttler;

import com.vmware.ph.phservice.common.server.throttler.RateLimiter;
import com.vmware.ph.phservice.push.telemetry.CollectorAgent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlatRateLimiterProvider implements RateLimiterProvider {
  private final ConcurrentMap<CollectorAgent, RateLimiter> _collectorAgentToRateLimiter;
  
  private final double _permitsPerSecond;
  
  private final Object _lock = new Object();
  
  public FlatRateLimiterProvider(double permitsPerSecond) {
    this._permitsPerSecond = permitsPerSecond;
    this._collectorAgentToRateLimiter = new ConcurrentHashMap<>();
  }
  
  public RateLimiter getRateLimiter(String collectorId, String collectorInstanceId) {
    CollectorAgent collectorAgent = new CollectorAgent(collectorId, collectorInstanceId);
    RateLimiter rateLimiter = this._collectorAgentToRateLimiter.get(collectorAgent);
    if (rateLimiter == null)
      synchronized (this._lock) {
        rateLimiter = this._collectorAgentToRateLimiter.get(collectorAgent);
        if (rateLimiter == null) {
          rateLimiter = new RateLimiter(this._permitsPerSecond);
          this._collectorAgentToRateLimiter.put(collectorAgent, rateLimiter);
        } 
      }  
    return rateLimiter;
  }
}
