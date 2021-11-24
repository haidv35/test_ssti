package com.vmware.ph.phservice.collector.core;

public class DefaultCollectorAgentProvider implements CollectorAgentProvider {
  private final CollectorAgentProvider.CollectorAgentId _agentId;
  
  public DefaultCollectorAgentProvider(String collectorId, String collectorInstanceId) {
    this._agentId = new CollectorAgentProvider.CollectorAgentId(collectorId, collectorInstanceId);
  }
  
  public CollectorAgentProvider.CollectorAgentId getCollectorAgentId() {
    return this._agentId;
  }
}
