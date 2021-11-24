package com.vmware.ph.phservice.push.telemetry;

import com.vmware.ph.phservice.common.ph.PhRtsClient;
import com.vmware.ph.phservice.common.ph.PhRtsClientFactory;
import com.vmware.ph.phservice.push.telemetry.internal.impl.ResultFuture;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PhRtsTelemetryService implements TelemetryService {
  private static final Log _log = LogFactory.getLog(PhRtsTelemetryService.class);
  
  private static final Set<Integer> SUCCESS_STATUS_CODES = new HashSet<>();
  
  private final PhRtsClientFactory _phRtsClientFactory;
  
  static {
    SUCCESS_STATUS_CODES.add(Integer.valueOf(200));
    SUCCESS_STATUS_CODES.add(Integer.valueOf(201));
    SUCCESS_STATUS_CODES.add(Integer.valueOf(202));
  }
  
  public PhRtsTelemetryService(PhRtsClientFactory phRtsClientFactory) {
    this._phRtsClientFactory = phRtsClientFactory;
  }
  
  public Future<Boolean> processTelemetry(String collectorId, String collectorInstanceId, TelemetryRequest[] telemetryRequests) {
    boolean isSuccessfullyProcessed = true;
    for (TelemetryRequest telemetryRequest : telemetryRequests) {
      isSuccessfullyProcessed = processTelemetry(telemetryRequest);
      if (!isSuccessfullyProcessed) {
        _log.error(
            String.format("Telemetry request for collector [%s] with instance [%s] could not be processed by VMware Analytics Cloud.", new Object[] { telemetryRequest.getCollectorId(), telemetryRequest
                .getCollectorIntanceId() }));
        break;
      } 
    } 
    return new ResultFuture<>(Boolean.valueOf(isSuccessfullyProcessed));
  }
  
  public boolean processTelemetry(TelemetryRequest telemetryRequest) {
    try (PhRtsClient phRtsClient = this._phRtsClientFactory.create()) {
      int statusCode = phRtsClient.send(telemetryRequest
          .getCollectorId(), telemetryRequest
          .getCollectorIntanceId(), null, telemetryRequest
          
          .getVersion(), telemetryRequest
          .getData(), telemetryRequest
          .isCompressed());
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("VMware Analytics Cloud returned status code [%d] in response to sending telemetry request.", new Object[] { Integer.valueOf(statusCode) })); 
      return SUCCESS_STATUS_CODES.contains(Integer.valueOf(statusCode));
    } 
  }
}
