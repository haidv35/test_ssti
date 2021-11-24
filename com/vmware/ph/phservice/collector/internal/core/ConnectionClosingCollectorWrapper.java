package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;

public class ConnectionClosingCollectorWrapper implements Collector {
  private final Collector _wrappedCollector;
  
  private final QueryServiceConnection _wrappedQueryServiceConnection;
  
  public ConnectionClosingCollectorWrapper(Collector wrappedCollector, QueryServiceConnection wrappedCollectorQueryService) {
    this._wrappedCollector = wrappedCollector;
    this._wrappedQueryServiceConnection = wrappedCollectorQueryService;
  }
  
  public void setContextData(Object contextData) {
    this._wrappedCollector.setContextData(contextData);
  }
  
  public void run() {
    this._wrappedCollector.run();
  }
  
  public CollectorOutcome collect() {
    return this._wrappedCollector.collect();
  }
  
  public void close() {
    this._wrappedCollector.close();
    this._wrappedQueryServiceConnection.close();
  }
}
