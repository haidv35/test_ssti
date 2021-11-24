package com.vmware.ph.phservice.push.telemetry;

import com.vmware.ph.phservice.push.telemetry.internal.impl.ResultFuture;
import java.util.Objects;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TelemetryLevelBasedTelemetryServiceWrapper implements TelemetryService {
  private static final Log _log = LogFactory.getLog(TelemetryLevelBasedTelemetryServiceWrapper.class);
  
  private final TelemetryLevelService _telemetryLevelService;
  
  protected final TelemetryService _wrappedTelemetryService;
  
  public TelemetryLevelBasedTelemetryServiceWrapper(TelemetryService wrappedTelemetryService, TelemetryLevelService telemetryLevelService) {
    this._wrappedTelemetryService = Objects.<TelemetryService>requireNonNull(wrappedTelemetryService);
    this._telemetryLevelService = Objects.<TelemetryLevelService>requireNonNull(telemetryLevelService);
  }
  
  public Future<Boolean> processTelemetry(String collectorId, String collectorInstanceId, TelemetryRequest[] telemetryRequests) {
    TelemetryLevel telemetryLevel = this._telemetryLevelService.getTelemetryLevel(collectorId, collectorInstanceId);
    boolean isSuccessfullyProcessed = false;
    if (telemetryLevel != TelemetryLevel.OFF) {
      Future<Boolean> result = this._wrappedTelemetryService.processTelemetry(collectorId, collectorInstanceId, telemetryRequests);
      try {
        isSuccessfullyProcessed = ((Boolean)result.get()).booleanValue();
      } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
        if (e instanceof InterruptedException)
          Thread.currentThread().interrupt(); 
      } 
    } 
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Telemetry request processed: %s; collector id: %s; telemetry level: %s", new Object[] { Boolean.valueOf(isSuccessfullyProcessed), collectorId, telemetryLevel
              
              .name() })); 
    return new ResultFuture<>(Boolean.valueOf(isSuccessfullyProcessed));
  }
}
