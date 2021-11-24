package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.cloud.dataapp.repository.AgentRepository;
import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.common.Pair;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DefaultDataAppAgentManager implements DataAppAgentManager {
  private final AgentRepository _agentRepository;
  
  private final DataApp _dataAppService;
  
  private final DataAppAgentFactory _agentFactory;
  
  private final List<DataAppAgent> _activeAgents = new LinkedList<>();
  
  public DefaultDataAppAgentManager(AgentRepository agentRepository, DataApp dataAppService, DataAppAgentFactory agentFactory) {
    this._agentRepository = agentRepository;
    this._dataAppService = dataAppService;
    this._agentFactory = agentFactory;
  }
  
  public synchronized DataAppAgent createAgent(DataAppAgentId agentId, DataAppAgentCreateSpec agentCreateSpec) throws DataAppAgentManager.AlreadyExists {
    Objects.requireNonNull(agentId);
    Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> repoAgentIdAndCreateSpecPair = this._agentRepository.get(agentId);
    if (repoAgentIdAndCreateSpecPair != null)
      throw new DataAppAgentManager.AlreadyExists(); 
    repoAgentIdAndCreateSpecPair = this._agentRepository.add(agentId, agentCreateSpec);
    DataAppAgent agent = startAgent(repoAgentIdAndCreateSpecPair
        .getFirst(), repoAgentIdAndCreateSpecPair
        .getSecond());
    return agent;
  }
  
  public synchronized void destroyAgent(DataAppAgentId agentId) throws DataAppAgentManager.NotFound {
    Objects.requireNonNull(agentId);
    Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> repoAgentIdAndCreateSpecPair = this._agentRepository.get(agentId);
    if (repoAgentIdAndCreateSpecPair == null)
      throw new DataAppAgentManager.NotFound(); 
    stopAgent(agentId);
    this._agentRepository.remove(agentId);
  }
  
  public DataAppAgent getAgent(DataAppAgentId agentId) throws DataAppAgentManager.NotFound, DataAppAgentManager.NotRunning {
    Objects.requireNonNull(agentId);
    Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> repoAgentIdAndCreateSpecPair = this._agentRepository.get(agentId);
    if (repoAgentIdAndCreateSpecPair == null)
      throw new DataAppAgentManager.NotFound(); 
    DataAppAgent agent = getActiveAgent(agentId);
    if (agent == null)
      throw new DataAppAgentManager.NotRunning(); 
    return agent;
  }
  
  public Set<DataAppAgent> getAgents() {
    return new HashSet<>(this._activeAgents);
  }
  
  public void init() {
    Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> repoAgentIdProviderToAgentSpec = this._agentRepository.list();
    for (Map.Entry<DataAppAgentIdProvider, DataAppAgentCreateInfo> entry : repoAgentIdProviderToAgentSpec.entrySet())
      startAgent(entry.getKey(), entry.getValue()); 
  }
  
  public synchronized void refresh() {
    Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> repoAgentIdProviderToAgentSpec = this._agentRepository.list();
    for (DataAppAgent activeAgent : this._activeAgents) {
      Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> repoAgentIdAndCreateSpecPair = this._agentRepository.get(activeAgent.getAgentId());
      if (repoAgentIdAndCreateSpecPair == null)
        stopAgent(activeAgent); 
    } 
    for (Map.Entry<DataAppAgentIdProvider, DataAppAgentCreateInfo> entry : repoAgentIdProviderToAgentSpec.entrySet()) {
      DataAppAgentIdProvider agentIdProvider = entry.getKey();
      DataAppAgentId agentId = agentIdProvider.getDataAppAgentId();
      DataAppAgent activeAgent = getActiveAgent(agentId);
      if (activeAgent == null)
        startAgent(agentIdProvider, entry.getValue()); 
    } 
  }
  
  public synchronized void close() {
    for (DataAppAgent agent : this._activeAgents)
      agent.close(); 
    this._activeAgents.clear();
  }
  
  private DataAppAgent startAgent(DataAppAgentIdProvider agentIdProvider, DataAppAgentCreateInfo agentSpec) {
    DataAppAgent agent = this._agentFactory.createAgent(this._dataAppService, agentIdProvider, agentSpec);
    this._activeAgents.add(agent);
    return agent;
  }
  
  private void stopAgent(DataAppAgentId agentId) {
    DataAppAgent agent = getActiveAgent(agentId);
    stopAgent(agent);
  }
  
  private void stopAgent(DataAppAgent agent) {
    if (agent != null) {
      agent.close();
      this._activeAgents.remove(agent);
    } 
  }
  
  private DataAppAgent getActiveAgent(DataAppAgentId agentId) {
    for (DataAppAgent activeAgent : this._activeAgents) {
      if (agentId.equals(activeAgent.getAgentId()))
        return activeAgent; 
    } 
    return null;
  }
}
