package com.vmware.ph.phservice.push.telemetry.internal.log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "TimeSinceLastRolloverTriggeringPolicy", category = "Core")
public class TimeSinceLastRolloverTriggeringPolicy extends AbstractTriggeringPolicy {
  private static final Log _log = LogFactory.getLog(TimeSinceLastRolloverTriggeringPolicy.class);
  
  static final String POLICY_NAME = "TimeSinceLastRolloverTriggeringPolicy";
  
  static final String INTERVAL_ATTR_NAME = "interval";
  
  static final long DEFAULT_ROLLOVER_INTERVAL = TimeUnit.DAYS.toMillis(1L);
  
  private final Configuration _configuration;
  
  private final long _rolloverIntervalMillis;
  
  private RollingFileManager _manager;
  
  @PluginFactory
  public static TimeSinceLastRolloverTriggeringPolicy createPolicy(@PluginConfiguration Configuration configuration, @PluginAttribute("interval") String interval) {
    long rolloverIntervalMillis = RolloverInterval.parse(interval, DEFAULT_ROLLOVER_INTERVAL);
    return new TimeSinceLastRolloverTriggeringPolicy(configuration, rolloverIntervalMillis);
  }
  
  private TimeSinceLastRolloverTriggeringPolicy(Configuration configuration, long rolloverIntervalMillis) {
    this._configuration = configuration;
    this._rolloverIntervalMillis = rolloverIntervalMillis;
  }
  
  public void initialize(RollingFileManager manager) {
    this._manager = manager;
    ConfigurationScheduler scheduler = this._configuration.getScheduler();
    if (!scheduler.isExecutorServiceSet())
      scheduler.incrementScheduledItems(); 
    if (!scheduler.isStarted())
      scheduler.start(); 
    scheduler.scheduleAtFixedRate(new RolloverRunnable(), this._rolloverIntervalMillis, this._rolloverIntervalMillis, TimeUnit.MILLISECONDS);
    _log.info(
        String.format("Initialized fixed-rate triggering policy with rollover interval of %d milliseconds", new Object[] { Long.valueOf(this._rolloverIntervalMillis) }));
  }
  
  public boolean isTriggeringEvent(LogEvent logEvent) {
    return false;
  }
  
  private boolean isTimeTriggeringEvent() {
    if (this._manager.getFileSize() == 0L) {
      if (_log.isDebugEnabled())
        _log.debug("Will not rollover empty log telemetry file."); 
      return false;
    } 
    long millisSinceLastRollover = System.currentTimeMillis() - this._manager.getFileTime();
    if (_log.isDebugEnabled())
      _log.debug(String.format("millisSinceLastRollover: %d, lastRollover: %d", new Object[] { Long.valueOf(millisSinceLastRollover), 
              Long.valueOf(this._manager.getFileTime()) })); 
    return (millisSinceLastRollover > this._rolloverIntervalMillis);
  }
  
  static class RolloverInterval {
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("([\\d]+)([smHhDd])");
    
    private static final Map<String, TimeUnit> TIMEUNITS = new HashMap<>(3);
    
    static {
      TIMEUNITS.put("s", TimeUnit.SECONDS);
      TIMEUNITS.put("m", TimeUnit.MINUTES);
      TIMEUNITS.put("h", TimeUnit.HOURS);
      TIMEUNITS.put("d", TimeUnit.DAYS);
    }
    
    static long parse(String rolloverIntervalPattern, long defaultRolloverIntervalMillis) {
      if (rolloverIntervalPattern == null)
        return defaultRolloverIntervalMillis; 
      Matcher intervalMatcher = INTERVAL_PATTERN.matcher(rolloverIntervalPattern);
      if (intervalMatcher.find()) {
        String timeValue = intervalMatcher.group(1);
        String timeUnitValue = intervalMatcher.group(2);
        if (timeValue == null || timeUnitValue == null)
          return defaultRolloverIntervalMillis; 
        TimeUnit timeUnit = TIMEUNITS.get(timeUnitValue.toLowerCase());
        return timeUnit.toMillis(Long.parseLong(timeValue));
      } 
      return defaultRolloverIntervalMillis;
    }
  }
  
  private class RolloverRunnable implements Runnable {
    private RolloverRunnable() {}
    
    public void run() {
      if (TimeSinceLastRolloverTriggeringPolicy._log.isDebugEnabled())
        TimeSinceLastRolloverTriggeringPolicy._log.debug(
            String.format("Running scheduled rollover of %s. Will check if log file matches the rollover requirements.", new Object[] { TimeSinceLastRolloverTriggeringPolicy.access$200(this.this$0).getFileName() })); 
      if (TimeSinceLastRolloverTriggeringPolicy.this.isTimeTriggeringEvent()) {
        if (TimeSinceLastRolloverTriggeringPolicy._log.isDebugEnabled())
          TimeSinceLastRolloverTriggeringPolicy._log.debug(
              String.format("Rolling over telemetry log file - %s.", new Object[] { TimeSinceLastRolloverTriggeringPolicy.access$200(this.this$0).getFileName() })); 
        TimeSinceLastRolloverTriggeringPolicy.this._manager.rollover();
      } 
    }
  }
}
