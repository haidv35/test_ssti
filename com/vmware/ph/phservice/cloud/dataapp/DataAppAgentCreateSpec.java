package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;

public class DataAppAgentCreateSpec {
  private ManifestSpec _manifestSpec;
  
  private String _objectType;
  
  private Boolean _isCollectionTriggerDataNeeded;
  
  private Boolean _isDeploymentDataNeeded;
  
  private Boolean _isResultNeeded;
  
  private Boolean _shouldSignalCollectionCompleted;
  
  private String _localManifestPath;
  
  private String _localPayloadPath;
  
  private String _localObfuscationMapPath;
  
  public DataAppAgentCreateSpec(ManifestSpec manifestSpec, String objectType, Boolean isCollectionTriggerDataNeeded, Boolean isDeploymentDataNeeded, Boolean isResultNeeded, Boolean shouldSignalCollectionCompleted, String localManifestPath, String localPayloadPath, String localObfuscationMapPath) {
    this._manifestSpec = manifestSpec;
    this._objectType = objectType;
    this._isCollectionTriggerDataNeeded = isCollectionTriggerDataNeeded;
    this._isDeploymentDataNeeded = isDeploymentDataNeeded;
    this._isResultNeeded = isResultNeeded;
    this._shouldSignalCollectionCompleted = shouldSignalCollectionCompleted;
    this._localManifestPath = localManifestPath;
    this._localPayloadPath = localPayloadPath;
    this._localObfuscationMapPath = localObfuscationMapPath;
  }
  
  public ManifestSpec getManifestSpec() {
    return this._manifestSpec;
  }
  
  public String getObjectType() {
    return this._objectType;
  }
  
  public Boolean isCollectionTriggerDataNeeded() {
    return this._isCollectionTriggerDataNeeded;
  }
  
  public Boolean isDeploymentDataNeeded() {
    return this._isDeploymentDataNeeded;
  }
  
  public Boolean isResultNeeded() {
    return this._isResultNeeded;
  }
  
  public Boolean shouldSignalCollectionCompleted() {
    return this._shouldSignalCollectionCompleted;
  }
  
  public String getLocalManifestPath() {
    return this._localManifestPath;
  }
  
  public String getLocalPayloadPath() {
    return this._localPayloadPath;
  }
  
  public String getLocalObfuscationMapPath() {
    return this._localObfuscationMapPath;
  }
}
