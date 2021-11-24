package com.vmware.ph.phservice.push.telemetry;

import com.vmware.ph.phservice.common.internal.CompositeFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MultiplexingTelemetryService implements TelemetryService {
  private final List<TelemetryService> _telemetryServiceList;
  
  public MultiplexingTelemetryService(List<TelemetryService> telemetryServiceList) {
    this._telemetryServiceList = telemetryServiceList;
  }
  
  public Future<Boolean> processTelemetry(String collectorId, String collectorInstanceId, TelemetryRequest[] telemetryRequests) {
    List<Future<Boolean>> telemetryServiceResults = new ArrayList<>(this._telemetryServiceList.size());
    for (TelemetryService telemetryService : this._telemetryServiceList) {
      Future<Boolean> uploadResult = telemetryService.processTelemetry(collectorId, collectorInstanceId, telemetryRequests);
      telemetryServiceResults.add(uploadResult);
    } 
    Future<Boolean> result = new MultiplexingTelemetryServiceFuture(telemetryServiceResults);
    return result;
  }
  
  private static class MultiplexingTelemetryServiceFuture implements Future<Boolean> {
    private final CompositeFuture<Boolean> _compositeFuture;
    
    public MultiplexingTelemetryServiceFuture(List<Future<Boolean>> telemetryServiceResults) {
      this._compositeFuture = new CompositeFuture<>(telemetryServiceResults);
    }
    
    public boolean cancel(boolean mayInterruptIfRunning) {
      return this._compositeFuture.cancel(mayInterruptIfRunning);
    }
    
    public boolean isCancelled() {
      return this._compositeFuture.isCancelled();
    }
    
    public boolean isDone() {
      return this._compositeFuture.isDone();
    }
    
    public Boolean get() throws InterruptedException, ExecutionException {
      List<Boolean> proccessedTelemetrySuccessResults = this._compositeFuture.get();
      return getIsSuccessfullyProccessed(proccessedTelemetrySuccessResults);
    }
    
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      List<Boolean> proccessedTelemetrySuccessResults = this._compositeFuture.get(timeout, unit);
      return getIsSuccessfullyProccessed(proccessedTelemetrySuccessResults);
    }
    
    private static Boolean getIsSuccessfullyProccessed(List<Boolean> proccessedTelemetrySuccessResults) {
      if (proccessedTelemetrySuccessResults.isEmpty())
        return Boolean.FALSE; 
      Boolean isSuccessfullyProcessed = Boolean.TRUE;
      for (Boolean proccessedTelemetrySuccessResult : proccessedTelemetrySuccessResults) {
        if (proccessedTelemetrySuccessResult.equals(Boolean.FALSE)) {
          isSuccessfullyProcessed = Boolean.FALSE;
          break;
        } 
      } 
      return isSuccessfullyProcessed;
    }
  }
}
