package com.vmware.ph.phservice.cloud.dataapp;

public interface DataAppAgentIdProvider {
  DataAppAgentId getDataAppAgentId();
  
  String getCollectorId();
  
  String getPluginType();
}
