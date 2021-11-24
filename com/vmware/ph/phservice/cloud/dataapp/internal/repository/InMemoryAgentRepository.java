package com.vmware.ph.phservice.cloud.dataapp.internal.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateInfo;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateSpec;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.DefaultDataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentRepository;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAgentRepository implements AgentRepository {
  private Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> _idProviderToSpec = new ConcurrentHashMap<>();
  
  public Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> add(DataAppAgentId agentId, final DataAppAgentCreateSpec agentCreateSpec) {
    DataAppAgentIdProvider idProvider = new DefaultDataAppAgentIdProvider(agentId);
    DataAppAgentCreateInfo createSpec = new DataAppAgentCreateInfo(new Builder<ManifestSpec>() {
          public ManifestSpec build() {
            return agentCreateSpec.getManifestSpec();
          }
        });
    this._idProviderToSpec.put(idProvider, createSpec);
    return new Pair<>(idProvider, createSpec);
  }
  
  public void remove(DataAppAgentId agentId) {
    DataAppAgentIdProvider idProvider = new DefaultDataAppAgentIdProvider(agentId);
    this._idProviderToSpec.remove(idProvider);
  }
  
  public Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> get(DataAppAgentId agentId) {
    DataAppAgentIdProvider idProvider = new DefaultDataAppAgentIdProvider(agentId);
    DataAppAgentCreateInfo createSpec = this._idProviderToSpec.get(idProvider);
    if (createSpec == null)
      return null; 
    return new Pair<>(idProvider, createSpec);
  }
  
  public Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> list() {
    return this._idProviderToSpec;
  }
}
