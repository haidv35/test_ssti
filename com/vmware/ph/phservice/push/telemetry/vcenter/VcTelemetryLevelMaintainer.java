package com.vmware.ph.phservice.push.telemetry.vcenter;

import com.vmware.ph.phservice.push.telemetry.CollectorAgent;
import com.vmware.ph.phservice.push.telemetry.TelemetryLevel;
import com.vmware.ph.phservice.push.telemetry.TelemetryLevelService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VcTelemetryLevelMaintainer {
  private static final Log _log = LogFactory.getLog(VcTelemetryLevelMaintainer.class);
  
  private static final String COLLECTOR_INSTANCE_ID_SEPARATOR = ".instance.";
  
  private final TelemetryLevelService _telemetryLevelService;
  
  private final VcLogManager _vcLogManager;
  
  private String _telemetryLogGroupPrefix;
  
  public VcTelemetryLevelMaintainer(TelemetryLevelService telemetryLevelService, VcLogManager vcLogManager, String telemetryLogGroupPrefix) {
    this._telemetryLevelService = telemetryLevelService;
    this._vcLogManager = vcLogManager;
    this._telemetryLogGroupPrefix = telemetryLogGroupPrefix;
  }
  
  public void adjustVcCollectors() {
    Map<String, VcLogManager.LogLevel> logNameToLogLevel = this._vcLogManager.getVcLogNameToLogLevel(this._telemetryLogGroupPrefix);
    Map<String, VcLogManager.LogLevel> logNameToNewLogLevel = calculateNewLogLevels(logNameToLogLevel);
    try {
      this._vcLogManager.setVcLogNameToLogLevel(logNameToNewLogLevel);
    } catch (Exception e) {
      _log.warn("Unable to set new log levels for vc telemetry loggers: ", e);
    } 
  }
  
  private Map<String, VcLogManager.LogLevel> calculateNewLogLevels(Map<String, VcLogManager.LogLevel> logNameToLogLevel) {
    if (logNameToLogLevel == null || logNameToLogLevel.isEmpty())
      return Collections.emptyMap(); 
    Map<String, VcLogManager.LogLevel> logNameToNewLogLevel = new HashMap<>();
    for (Map.Entry<String, VcLogManager.LogLevel> entry : logNameToLogLevel.entrySet()) {
      String logName = entry.getKey();
      VcLogManager.LogLevel logLevel = entry.getValue();
      CollectorAgent collectorAgent = convertLogNameToCollectorAgent(logName);
      if (collectorAgent != null) {
        TelemetryLevel telemetryLevel = this._telemetryLevelService.getTelemetryLevel(collectorAgent
            .getCollectorId(), collectorAgent
            .getCollectorInstanceId());
        VcLogManager.LogLevel newLogLevel = convertTelemetryLevelToLogLevel(telemetryLevel);
        if (newLogLevel != logLevel)
          logNameToNewLogLevel.put(logName, newLogLevel); 
        continue;
      } 
      if (_log.isWarnEnabled())
        _log.warn("Cannot infer Collector from the given logger name: " + logName); 
    } 
    return logNameToNewLogLevel;
  }
  
  private CollectorAgent convertLogNameToCollectorAgent(String logName) {
    String collectorId = null;
    String collectorInstanceId = null;
    if (logName == null || 
      !logName.contains(this._telemetryLogGroupPrefix))
      return null; 
    String logNameWithoutPrefixes = logName.substring(logName
        .indexOf(this._telemetryLogGroupPrefix) + this._telemetryLogGroupPrefix
        .length());
    int lastSeparatorIndex = logNameWithoutPrefixes.lastIndexOf(".instance.");
    if (!isSeparatorFound(lastSeparatorIndex))
      return null; 
    collectorId = logNameWithoutPrefixes.substring(0, lastSeparatorIndex);
    collectorInstanceId = logNameWithoutPrefixes.substring(lastSeparatorIndex + ".instance."
        .length());
    if (collectorInstanceId.length() == 0)
      return null; 
    return new CollectorAgent(collectorId, collectorInstanceId);
  }
  
  private static boolean isSeparatorFound(int lastSeparatorIndex) {
    return (lastSeparatorIndex != -1);
  }
  
  static VcLogManager.LogLevel convertTelemetryLevelToLogLevel(TelemetryLevel telemetryLevel) {
    if (telemetryLevel == TelemetryLevel.FULL)
      return VcLogManager.LogLevel.verbose; 
    if (telemetryLevel == TelemetryLevel.BASIC)
      return VcLogManager.LogLevel.info; 
    return VcLogManager.LogLevel.none;
  }
}
