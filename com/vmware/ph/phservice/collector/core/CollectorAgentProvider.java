package com.vmware.ph.phservice.collector.core;

public interface CollectorAgentProvider {
  CollectorAgentId getCollectorAgentId();
  
  public static class CollectorAgentId {
    private final String _collectorId;
    
    private final String _collectorInstanceId;
    
    public CollectorAgentId(String collectorId, String collectorInstanceId) {
      this._collectorId = collectorId;
      this._collectorInstanceId = collectorInstanceId;
    }
    
    public String getCollectorId() {
      return this._collectorId;
    }
    
    public String getCollectorInstanceId() {
      return this._collectorInstanceId;
    }
  }
}
