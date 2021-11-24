package com.vmware.ph.phservice.cloud.dataapp.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateInfo;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateSpec;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.common.Pair;
import java.util.Map;

public interface AgentRepository {
  Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> add(DataAppAgentId paramDataAppAgentId, DataAppAgentCreateSpec paramDataAppAgentCreateSpec);
  
  void remove(DataAppAgentId paramDataAppAgentId);
  
  Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> list();
  
  Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> get(DataAppAgentId paramDataAppAgentId);
}
