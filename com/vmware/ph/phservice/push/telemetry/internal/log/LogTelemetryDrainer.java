package com.vmware.ph.phservice.push.telemetry.internal.log;

import com.vmware.ph.phservice.push.telemetry.CollectorAgent;
import com.vmware.ph.phservice.push.telemetry.TelemetryRequest;
import com.vmware.ph.phservice.push.telemetry.TelemetryService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogTelemetryDrainer {
  private static final Log _log = LogFactory.getLog(LogTelemetryDrainer.class);
  
  private final LogTelemetryDrainContextProvider _logTelemetryDrainContextProvider;
  
  private final TelemetryService _nextTelemetryService;
  
  public LogTelemetryDrainer(LogTelemetryDrainContextProvider logTelemetryDrainContextProvider, TelemetryService nextTelemetryService) {
    this._logTelemetryDrainContextProvider = logTelemetryDrainContextProvider;
    this._nextTelemetryService = nextTelemetryService;
  }
  
  public void drain(String collectorId, String collectorInstanceId) {
    _log.debug(
        String.format("Start draining for collector: %s and collectorInstanceId: %s", new Object[] { collectorId, collectorInstanceId }));
    LogTelemetryDrainContext context = this._logTelemetryDrainContextProvider.getContext(collectorId, collectorInstanceId);
    _log.debug(
        String.format("Drain context for collector: %s and collectorInstanceId: %s is: %s", new Object[] { collectorId, collectorInstanceId, context }));
    try {
      List<Path> nonProcessedLogFilePaths = context.getNonProcessedLogFilePaths();
      _log.debug(
          String.format("Non processed log file paths for collector: %s and collectorInstanceId: %s are: %s", new Object[] { collectorId, collectorInstanceId, nonProcessedLogFilePaths }));
      for (Path logFilePath : nonProcessedLogFilePaths) {
        boolean isSuccessfullyProcessed = processLog(collectorId, collectorInstanceId, logFilePath);
        if (isSuccessfullyProcessed) {
          _log.debug(
              String.format("Successfully processed log file: %s for collectorId: %s and collectorInstanceId: %s ", new Object[] { logFilePath, collectorId, collectorInstanceId }));
          context.markLogFilePathAsProcessed(logFilePath);
        } 
      } 
    } finally {
      _log.debug(
          String.format("Update context for collectorId: %s and collectorInstanceId: %s with: %s", new Object[] { collectorId, collectorInstanceId, context }));
      this._logTelemetryDrainContextProvider.updateContext(context);
    } 
  }
  
  public List<CollectorAgent> getAvailableCollectors() {
    return this._logTelemetryDrainContextProvider.getAvailableCollectors();
  }
  
  private boolean processLog(String collectorId, String collectorInstanceId, Path logFilePath) {
    if (!LogTelemetryUtil.isCompressed(logFilePath))
      return false; 
    boolean isSuccessfullyProcessed = false;
    try {
      TelemetryRequest telemetryRequest = LogTelemetryUtil.buildTelemetryRequest(collectorId, collectorInstanceId, logFilePath);
      Future<Boolean> result = this._nextTelemetryService.processTelemetry(collectorId, collectorInstanceId, new TelemetryRequest[] { telemetryRequest });
      isSuccessfullyProcessed = ((Boolean)result.get()).booleanValue();
    } catch (IOException e) {
      _log.error("Could not process log file " + logFilePath, e);
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return isSuccessfullyProcessed;
  }
}
