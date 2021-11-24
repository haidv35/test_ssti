package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;

public class AgentCollectorAgentProvider implements CollectorAgentProvider {
  private final DataAppAgentId _agentId;
  
  public AgentCollectorAgentProvider(DataAppAgentId agentId) {
    this._agentId = agentId;
  }
  
  public CollectorAgentProvider.CollectorAgentId getCollectorAgentId() {
    CollectorAgentProvider.CollectorAgentId agentId = new CollectorAgentProvider.CollectorAgentId(this._agentId.getCollectorId(), this._agentId.getCollectorInstanceId());
    return agentId;
  }
}
