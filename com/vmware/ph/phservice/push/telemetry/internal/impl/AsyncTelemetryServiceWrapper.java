package com.vmware.ph.phservice.push.telemetry.internal.impl;

import com.vmware.ph.phservice.push.telemetry.TelemetryRequest;
import com.vmware.ph.phservice.push.telemetry.TelemetryService;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncTelemetryServiceWrapper implements TelemetryService, Closeable {
  private final TelemetryService _telemetryService;
  
  private final ExecutorService _telemetryRequestsExecutor;
  
  public AsyncTelemetryServiceWrapper(TelemetryService telemetryService) {
    this._telemetryService = telemetryService;
    this._telemetryRequestsExecutor = Executors.newCachedThreadPool();
  }
  
  public Future<Boolean> processTelemetry(String collectorId, String collectorInstanceId, TelemetryRequest[] telemetryRequests) {
    this._telemetryRequestsExecutor.submit(new TelemetryRequestProcessorRunnable(collectorId, collectorInstanceId, telemetryRequests));
    return new ResultFuture<>(Boolean.TRUE);
  }
  
  public void close() {
    this._telemetryRequestsExecutor.shutdown();
  }
  
  private class TelemetryRequestProcessorRunnable implements Runnable {
    private final String _collectorId;
    
    private final String _collectorInstanceId;
    
    private final TelemetryRequest[] _telemetryRequests;
    
    public TelemetryRequestProcessorRunnable(String collectorId, String collectorInstanceId, TelemetryRequest[] telemetryRequests) {
      this._collectorId = collectorId;
      this._collectorInstanceId = collectorInstanceId;
      this._telemetryRequests = telemetryRequests;
    }
    
    public void run() {
      AsyncTelemetryServiceWrapper.this._telemetryService.processTelemetry(this._collectorId, this._collectorInstanceId, this._telemetryRequests);
    }
  }
}
