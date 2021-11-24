package com.vmware.ph.phservice.collector.scheduler;

import java.util.concurrent.TimeUnit;

public class DefaultCollectorLoopExecutionConfigProvider implements CollectorLoopExecutionConfigProvider {
  static final long DEFAULT_INIT_BACKOFF_TIME_SECONDS = TimeUnit.MINUTES
    .toSeconds(5L);
  
  static final long DEFAULT_BACKOFF_TIME_ONERROR_SECONDS = TimeUnit.MINUTES
    .toSeconds(1L);
  
  static final long DEFAULT_SCHEDULE_CHECK_TIME_SECONDS = TimeUnit.MINUTES
    .toSeconds(1L);
  
  static final long DEFAULT_COLLECTOR_THREAD_TIMEOUT_SECONDS = TimeUnit.HOURS
    .toSeconds(6L);
  
  static final long DEFAULT_COLLECTOR_LOOP_TIMEOUT_SECONDS = 0L;
  
  private long _initialBackOffTimeSeconds = DEFAULT_INIT_BACKOFF_TIME_SECONDS;
  
  private long _backOffTimeSeconds = DEFAULT_BACKOFF_TIME_ONERROR_SECONDS;
  
  private long _scheduleCheckTimeSeconds = DEFAULT_SCHEDULE_CHECK_TIME_SECONDS;
  
  private long _collectionThreadTimeoutSeconds = DEFAULT_COLLECTOR_THREAD_TIMEOUT_SECONDS;
  
  private long _collectorLoopTimeoutSeconds = 0L;
  
  private boolean _shouldRunOnce = false;
  
  public DefaultCollectorLoopExecutionConfigProvider() {}
  
  public DefaultCollectorLoopExecutionConfigProvider(long initialBackOffTimeSeconds, long backOffTimeSeconds, long scheduleCheckTimeSeconds, long collectionThreadTimeoutSeconds, long singleCollectionExecutionTimeoutSeconds, boolean shouldRunOnce) {
    this._initialBackOffTimeSeconds = initialBackOffTimeSeconds;
    this._backOffTimeSeconds = backOffTimeSeconds;
    this._scheduleCheckTimeSeconds = scheduleCheckTimeSeconds;
    this._collectionThreadTimeoutSeconds = collectionThreadTimeoutSeconds;
    this._collectorLoopTimeoutSeconds = singleCollectionExecutionTimeoutSeconds;
    this._shouldRunOnce = shouldRunOnce;
  }
  
  public long getInitialBackOffTimeSeconds() {
    return this._initialBackOffTimeSeconds;
  }
  
  public long getBackoffTimeSeconds() {
    return this._backOffTimeSeconds;
  }
  
  public long getScheduleCheckTimeSeconds() {
    return this._scheduleCheckTimeSeconds;
  }
  
  public boolean shouldRunOnce() {
    return this._shouldRunOnce;
  }
  
  public long getCollectorThreadTimeoutSeconds() {
    return this._collectionThreadTimeoutSeconds;
  }
  
  public long getCollectorLoopTimeoutSeconds() {
    return this._collectorLoopTimeoutSeconds;
  }
}
