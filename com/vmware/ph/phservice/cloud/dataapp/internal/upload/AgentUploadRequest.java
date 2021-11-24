package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;

public abstract class AgentUploadRequest {
  private final String _collectorId;
  
  private final String _collectorInstanceId;
  
  private final String _collectionId;
  
  private final String _deploymentSecret;
  
  private final String _objectId;
  
  private final PluginTypeContext _pluginContext;
  
  public AgentUploadRequest(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, String objectId, PluginTypeContext pluginContext) {
    this._collectorId = collectorId;
    this._collectorInstanceId = collectorInstanceId;
    this._collectionId = collectionId;
    this._deploymentSecret = deploymentSecret;
    this._objectId = objectId;
    this._pluginContext = pluginContext;
  }
  
  public String getCollectorId() {
    return this._collectorId;
  }
  
  public String getCollectorInstanceId() {
    return this._collectorInstanceId;
  }
  
  public String getCollectionId() {
    return this._collectionId;
  }
  
  public String getDeploymentSecret() {
    return this._deploymentSecret;
  }
  
  public String getObjectId() {
    return this._objectId;
  }
  
  public PluginTypeContext getPluginContext() {
    return this._pluginContext;
  }
}
