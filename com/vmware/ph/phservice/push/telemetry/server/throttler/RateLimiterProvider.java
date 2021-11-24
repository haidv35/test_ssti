package com.vmware.ph.phservice.push.telemetry.server.throttler;

import com.vmware.ph.phservice.common.server.throttler.RateLimiter;

public interface RateLimiterProvider {
  RateLimiter getRateLimiter(String paramString1, String paramString2);
}
