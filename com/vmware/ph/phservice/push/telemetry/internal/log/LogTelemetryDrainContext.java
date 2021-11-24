package com.vmware.ph.phservice.push.telemetry.internal.log;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

class LogTelemetryDrainContext {
  private final String _collectorId;
  
  private final String _collectorInstanceId;
  
  private final List<Path> _timeSortedLogFilePaths;
  
  private final Map<Path, Date> _logFilePathToLastProcessedDate;
  
  public LogTelemetryDrainContext(String collectorId, String collectorInstanceId, List<Path> timeSortedLogFilePaths, Map<Path, Date> logFilePathToLastProcessedDate) {
    this._collectorId = collectorId;
    this._collectorInstanceId = collectorInstanceId;
    this._timeSortedLogFilePaths = timeSortedLogFilePaths;
    this._logFilePathToLastProcessedDate = logFilePathToLastProcessedDate;
  }
  
  public String getCollectorId() {
    return this._collectorId;
  }
  
  public String getCollectorInstanceId() {
    return this._collectorInstanceId;
  }
  
  public List<Path> getNonProcessedLogFilePaths() {
    List<Path> notProcessedLogFilePaths = new ArrayList<>();
    for (Path logFilePath : this._timeSortedLogFilePaths) {
      Date lastProcessedTimestamp = this._logFilePathToLastProcessedDate.get(logFilePath);
      if (LogTelemetryUtil.isLogFileNotProcessed(logFilePath, lastProcessedTimestamp))
        notProcessedLogFilePaths.add(logFilePath); 
    } 
    return notProcessedLogFilePaths;
  }
  
  public void markLogFilePathAsProcessed(Path logFilePath) {
    Date nowTimestamp = new Date();
    this._logFilePathToLastProcessedDate.put(logFilePath, nowTimestamp);
  }
  
  public Map<Path, Date> getLogFilePathToLastProcessedDate() {
    return this._logFilePathToLastProcessedDate;
  }
  
  public String toString() {
    return "LogTelemetryDrainContext{_collectorId='" + this._collectorId + '\'' + ", _collectorInstanceId='" + this._collectorInstanceId + '\'' + ", _timeSortedLogFilePaths=" + this._timeSortedLogFilePaths + ", _logFilePathToLastProcessedDate=" + this._logFilePathToLastProcessedDate + '}';
  }
}
