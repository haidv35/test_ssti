package com.vmware.ph.phservice.collector.internal.manifest.scheduling;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScheduleIntervalParser {
  private static final Log _log = LogFactory.getLog(ScheduleIntervalParser.class);
  
  private static final String INTERVAL_REGEX = "([\\d]+)([mhdw])";
  
  public static long parseIntervalMillis(String intervalStr) {
    if (intervalStr == null || !intervalStr.matches("([\\d]+)([mhdw])")) {
      if (_log.isDebugEnabled())
        _log.debug("Invalid interval string. Returning 0"); 
      return 0L;
    } 
    String intervalValue = intervalStr.replaceAll("([\\d]+)([mhdw])", "$1");
    String timeUnitValue = intervalStr.replaceAll("([\\d]+)([mhdw])", "$2");
    long interval = Long.parseLong(intervalValue);
    if (interval <= 0L) {
      if (_log.isDebugEnabled())
        _log.debug("Negative interval values are not allowed. Returning 0"); 
      return 0L;
    } 
    switch (timeUnitValue) {
      case "w":
        return TimeUnit.DAYS.toMillis(interval * 7L);
      case "d":
        return TimeUnit.DAYS.toMillis(interval);
      case "h":
        return TimeUnit.HOURS.toMillis(interval);
      case "m":
        return TimeUnit.MINUTES.toMillis(interval);
    } 
    if (_log.isDebugEnabled())
      _log.debug(timeUnitValue + " is an invalid time unit specifier. Returning 0"); 
    return 0L;
  }
}
