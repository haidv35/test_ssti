package com.vmware.ph.phservice.push.telemetry.internal.log;

import com.vmware.ph.phservice.push.telemetry.CollectorAgent;
import com.vmware.ph.phservice.push.telemetry.TelemetryLevel;
import com.vmware.ph.phservice.push.telemetry.TelemetryLevelService;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.AsyncTaskExecutor;

public class LogTelemetryDrainerExecutor {
  private static final Log _log = LogFactory.getLog(LogTelemetryDrainerExecutor.class);
  
  private final LogTelemetryDrainer _logTelemetryDrainer;
  
  private final AsyncTaskExecutor _asyncTaskExecutor;
  
  private final TelemetryLevelService _telemetryLevelService;
  
  public LogTelemetryDrainerExecutor(LogTelemetryDrainer logTelemetryDrainer, AsyncTaskExecutor asyncTaskExecutor, TelemetryLevelService telemetryLevelService) {
    this._logTelemetryDrainer = logTelemetryDrainer;
    this._asyncTaskExecutor = asyncTaskExecutor;
    this._telemetryLevelService = telemetryLevelService;
  }
  
  public synchronized void execute() {
    List<CollectorAgent> availableCollectors = this._logTelemetryDrainer.getAvailableCollectors();
    _log.debug("Available collectors are: " + availableCollectors);
    for (CollectorAgent collectorAgent : availableCollectors) {
      this._asyncTaskExecutor.execute(new Runnable() {
            public void run() {
              LogTelemetryDrainerExecutor._log.debug("Beginning run...");
              String collectorId = collectorAgent.getCollectorId();
              String collectorInstanceId = collectorAgent.getCollectorInstanceId();
              TelemetryLevel telemetryLevel = LogTelemetryDrainerExecutor.this._telemetryLevelService.getTelemetryLevel(collectorId, collectorInstanceId);
              LogTelemetryDrainerExecutor._log.debug(
                  String.format("Telemetry Level for collectorId: %s and collectorInstanceId: %s is: %s", new Object[] { collectorId, collectorInstanceId, telemetryLevel }));
              if (!TelemetryLevel.OFF.equals(telemetryLevel)) {
                LogTelemetryDrainerExecutor.this._logTelemetryDrainer.drain(collectorId, collectorInstanceId);
              } else {
                LogTelemetryDrainerExecutor._log.debug(
                    String.format("Draining skipped for collectorId=%s and collectorInstanceId=%s, because the telemetry level is OFF.", new Object[] { collectorId, collectorInstanceId }));
              } 
            }
          });
    } 
  }
}
