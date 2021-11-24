package com.vmware.ph.phservice.cloud.dataapp.internal;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;

public class DefaultDataAppAgentIdProvider implements DataAppAgentIdProvider {
  private final DataAppAgentId _agentId;
  
  public DefaultDataAppAgentIdProvider(DataAppAgentId agentId) {
    this._agentId = agentId;
  }
  
  public DataAppAgentId getDataAppAgentId() {
    return this._agentId;
  }
  
  public String getCollectorId() {
    return (this._agentId != null) ? this._agentId.getCollectorId() : null;
  }
  
  public String getPluginType() {
    return (this._agentId != null) ? this._agentId.getPluginType() : null;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof DefaultDataAppAgentIdProvider))
      return false; 
    DefaultDataAppAgentIdProvider other = (DefaultDataAppAgentIdProvider)obj;
    return this._agentId.equals(other._agentId);
  }
  
  public int hashCode() {
    return this._agentId.hashCode();
  }
}
