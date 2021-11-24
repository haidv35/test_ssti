package com.vmware.ph.phservice.push.telemetry;

import com.vmware.ph.phservice.push.telemetry.internal.impl.ResultFuture;
import com.vmware.ph.phservice.push.telemetry.internal.log.LogTelemetryUtil;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;

public class LogTelemetryService implements TelemetryService {
  private static final String CTX_LOG_TELEMETRY_DIR_PATH = "logTelemetryDirPath";
  
  private static final String CTX_LOG_TELEMETRY_FILE_NAME = "logTelemetryFileName";
  
  private final Path _logDirPath;
  
  private final Logger _logger;
  
  public LogTelemetryService(Path logDirPath, LoggerContext loggerContext) {
    this._logDirPath = logDirPath;
    this._logger = (Logger)loggerContext.getLogger(LogTelemetryService.class
        .getName() + ":" + logDirPath.toString());
  }
  
  public Future<Boolean> processTelemetry(String collectorId, String collectorInstanceId, TelemetryRequest[] telemetryRequests) {
    ThreadContext.put("logTelemetryDirPath", this._logDirPath
        .normalize().toString());
    ThreadContext.put("logTelemetryFileName", 
        
        LogTelemetryUtil.getLogFileNamePattern(collectorId, collectorInstanceId));
    for (TelemetryRequest telemetryRequest : telemetryRequests)
      this._logger.info(serializeToLogMessage(telemetryRequest)); 
    return new ResultFuture<>(Boolean.TRUE);
  }
  
  private static String serializeToLogMessage(TelemetryRequest telemetryRequest) {
    String body = "";
    try {
      body = new String(telemetryRequest.getData(), "UTF-8");
    } catch (UnsupportedEncodingException unsupportedEncodingException) {}
    return body;
  }
}
