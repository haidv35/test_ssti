package com.vmware.ph.phservice.cloud.dataapp.server;

import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.common.internal.JsonUtil;
import java.util.Map;

public class DataAppAgentResponseSerializer {
  public static String serializeDiagnosticData(AgentStatus agentStatus) {
    return convertObjectToJsonString(agentStatus);
  }
  
  public static String serializeObfuscationMap(Map<String, String> obuscationMap) {
    return convertObjectToJsonString(obuscationMap);
  }
  
  private static String convertObjectToJsonString(Object object) {
    return (object != null) ? JsonUtil.toJson(object) : "";
  }
}
