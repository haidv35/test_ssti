package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import java.util.Map;
import java.util.Set;

public interface CollectorDataAppAgent extends DataAppAgent {
  Iterable<PluginResult> execute(Map<String, Object> paramMap, AgentResultRetriever paramAgentResultRetriever, CollectionSchedule paramCollectionSchedule);
  
  String collect(String paramString1, String paramString2, Object paramObject);
  
  AgentCollectionScheduleSpec getCollectionScheduleSpec(Set<String> paramSet);
  
  Map<String, String> exportObfuscationMap(String paramString);
}
