package com.vmware.ph.phservice.cloud.dataapp.internal.util;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.vapi.internal.util.StringUtils;

public class AgentPropertyNameUtil {
  public static String getAgentSpecificPropertyName(DataAppAgentIdProvider agentIdProvider, String propertyName) {
    if (propertyName == null || "".equals(propertyName))
      return propertyName; 
    StringBuilder agentPropertyName = new StringBuilder(propertyName);
    String collectorId = agentIdProvider.getCollectorId();
    if (StringUtils.isNotBlank(collectorId)) {
      agentPropertyName.append(".");
      agentPropertyName.append(collectorId);
      String pluginType = agentIdProvider.getPluginType();
      if (StringUtils.isNotBlank(pluginType)) {
        agentPropertyName.append(".");
        agentPropertyName.append(pluginType);
      } 
    } 
    return agentPropertyName.toString();
  }
}
