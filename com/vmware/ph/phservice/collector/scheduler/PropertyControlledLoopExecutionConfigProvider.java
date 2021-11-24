package com.vmware.ph.phservice.collector.scheduler;

import com.vmware.ph.phservice.common.internal.ConfigurationService;
import org.apache.commons.lang3.StringUtils;

public class PropertyControlledLoopExecutionConfigProvider implements CollectorLoopExecutionConfigProvider {
  private final ConfigurationService _configurationService;
  
  private final String _propNameForInitBackOffTime;
  
  private final String _propNameForScheduleCheckTime;
  
  private final String _propNameForBackOffTimeOnError;
  
  private final String _propNameForCollectorThreadTimeout;
  
  private final String _propNameForCollectorLoopTimeoutSeconds;
  
  private final String _propNameForRunCollectorOnce;
  
  private final long _defaultInitBackoffTimeSeconds;
  
  private final long _defaultBackoffTimeOnErrorSeconds;
  
  private final long _defaultScheduleCheckTimeSeconds;
  
  private final long _defaultCollectorThreadTimeoutSeconds;
  
  private final long _defaultCollectorLoopTimeoutSeconds;
  
  public PropertyControlledLoopExecutionConfigProvider(ConfigurationService configurationService, String propNameForInitBackOffTime, String propNameForScheduleCheckTime, String propNameForBackOffTimeOnError, String propNameForCollectorThreadTimeout, String propNameForCollectorLoopTimeoutSeconds, String propNameForRunCollectorOnce) {
    this(configurationService, propNameForInitBackOffTime, propNameForScheduleCheckTime, propNameForBackOffTimeOnError, propNameForCollectorThreadTimeout, propNameForCollectorLoopTimeoutSeconds, propNameForRunCollectorOnce, DefaultCollectorLoopExecutionConfigProvider.DEFAULT_INIT_BACKOFF_TIME_SECONDS, DefaultCollectorLoopExecutionConfigProvider.DEFAULT_SCHEDULE_CHECK_TIME_SECONDS, DefaultCollectorLoopExecutionConfigProvider.DEFAULT_BACKOFF_TIME_ONERROR_SECONDS, DefaultCollectorLoopExecutionConfigProvider.DEFAULT_COLLECTOR_THREAD_TIMEOUT_SECONDS, 0L);
  }
  
  public PropertyControlledLoopExecutionConfigProvider(ConfigurationService configurationService, String propNameForInitBackOffTime, String propNameForScheduleCheckTime, String propNameForBackOffTimeOnError, String propNameForCollectorThreadTimeout, String propNameForCollectorLoopTimeoutSeconds, String propNameForRunCollectorOnce, long defaultInitBackoffTimeSeconds, long defaultScheduleCheckTimeSeconds, long defaultBackoffTimeOnErrorSeconds, long defaultCollectorThreadTimeoutSeconds, long defaultCollectorLoopTimeoutSeconds) {
    this._configurationService = configurationService;
    this._propNameForInitBackOffTime = propNameForInitBackOffTime;
    this._propNameForScheduleCheckTime = propNameForScheduleCheckTime;
    this._propNameForBackOffTimeOnError = propNameForBackOffTimeOnError;
    this._propNameForCollectorThreadTimeout = propNameForCollectorThreadTimeout;
    this._propNameForCollectorLoopTimeoutSeconds = propNameForCollectorLoopTimeoutSeconds;
    this._propNameForRunCollectorOnce = propNameForRunCollectorOnce;
    this._defaultInitBackoffTimeSeconds = defaultInitBackoffTimeSeconds;
    this._defaultScheduleCheckTimeSeconds = defaultScheduleCheckTimeSeconds;
    this._defaultBackoffTimeOnErrorSeconds = defaultBackoffTimeOnErrorSeconds;
    this._defaultCollectorThreadTimeoutSeconds = defaultCollectorThreadTimeoutSeconds;
    this._defaultCollectorLoopTimeoutSeconds = defaultCollectorLoopTimeoutSeconds;
  }
  
  public long getInitialBackOffTimeSeconds() {
    return getLongValue(this._propNameForInitBackOffTime, this._defaultInitBackoffTimeSeconds);
  }
  
  public long getBackoffTimeSeconds() {
    return getLongValue(this._propNameForBackOffTimeOnError, this._defaultBackoffTimeOnErrorSeconds);
  }
  
  public long getScheduleCheckTimeSeconds() {
    return getLongValue(this._propNameForScheduleCheckTime, this._defaultScheduleCheckTimeSeconds);
  }
  
  public boolean shouldRunOnce() {
    Boolean rawValue = this._configurationService.getBoolProperty(this._propNameForRunCollectorOnce);
    return (rawValue == null) ? false : rawValue.booleanValue();
  }
  
  public long getCollectorThreadTimeoutSeconds() {
    return getLongValue(this._propNameForCollectorThreadTimeout, this._defaultCollectorThreadTimeoutSeconds);
  }
  
  public long getCollectorLoopTimeoutSeconds() {
    return getLongValue(this._propNameForCollectorLoopTimeoutSeconds, this._defaultCollectorLoopTimeoutSeconds);
  }
  
  private long getLongValue(String propertyName, long defaultValue) {
    if (StringUtils.isBlank(propertyName))
      return defaultValue; 
    Long value = this._configurationService.getLongProperty(propertyName);
    return (value == null) ? defaultValue : value.longValue();
  }
}
