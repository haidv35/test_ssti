package com.vmware.ph.phservice.cloud.dataapp.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import java.util.Map;

public interface AgentObfuscationRepository {
  Map<String, String> readObfuscationMap(DataAppAgentId paramDataAppAgentId, String paramString);
  
  void writeObfuscationMap(DataAppAgentId paramDataAppAgentId, String paramString, Map<String, String> paramMap);
}
