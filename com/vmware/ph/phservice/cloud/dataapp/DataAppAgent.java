package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;

public interface DataAppAgent {
  DataAppAgentId getAgentId();
  
  PluginResult execute(String paramString, Object paramObject, boolean paramBoolean);
  
  PluginResult execute(String paramString, Object paramObject, ProgressReporter paramProgressReporter);
  
  AgentStatus getAgentStatus();
  
  void close();
}
