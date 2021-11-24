package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import java.util.Objects;

public class AgentJsonUploadRequest extends AgentUploadRequest {
  private final String _jsonData;
  
  public AgentJsonUploadRequest(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, String objectId, PluginTypeContext pluginContext, String jsonData) {
    super(collectorId, collectorInstanceId, collectionId, deploymentSecret, objectId, pluginContext);
    this._jsonData = Objects.<String>requireNonNull(jsonData);
  }
  
  public String getJson() {
    return this._jsonData;
  }
}
