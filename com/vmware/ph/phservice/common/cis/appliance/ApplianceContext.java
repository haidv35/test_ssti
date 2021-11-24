package com.vmware.ph.phservice.common.cis.appliance;

public class ApplianceContext {
  private final String _id;
  
  private final String _hostName;
  
  private final String _absoluteHostName;
  
  private final String _nodeId;
  
  private final String _domainId;
  
  private final ApplianceCredentialsProvider _credentialsProvider;
  
  private DeploymentNodeTypeReader.DeploymentNodeType _deploymentNodeType = DeploymentNodeTypeReader.DeploymentNodeType.NONE;
  
  private boolean _isLocal;
  
  public ApplianceContext(String id, String hostName, String nodeId, String domainId, ApplianceCredentialsProvider credentialsProvider) {
    this._id = id;
    this._hostName = hostName;
    this._absoluteHostName = null;
    this._nodeId = nodeId;
    this._domainId = domainId;
    this._credentialsProvider = credentialsProvider;
  }
  
  public ApplianceContext(String id, String hostName, String absoluteHostName, String nodeId, String domainId, ApplianceCredentialsProvider credentialsProvider) {
    this._id = id;
    this._hostName = hostName;
    this._absoluteHostName = absoluteHostName;
    this._nodeId = nodeId;
    this._domainId = domainId;
    this._credentialsProvider = credentialsProvider;
  }
  
  public void setDeploymentNodeType(DeploymentNodeTypeReader.DeploymentNodeType deploymentNodeType) {
    this._deploymentNodeType = deploymentNodeType;
  }
  
  public void setLocal(boolean isLocal) {
    this._isLocal = isLocal;
  }
  
  public String getId() {
    return this._id;
  }
  
  public String getHostName() {
    return this._hostName;
  }
  
  public String getAbsoluteHostName() {
    return this._absoluteHostName;
  }
  
  public String getNodeId() {
    return this._nodeId;
  }
  
  public String getDomainId() {
    return this._domainId;
  }
  
  public ApplianceCredentialsProvider getCredentialsProvider() {
    return this._credentialsProvider;
  }
  
  public DeploymentNodeTypeReader.DeploymentNodeType getDeploymentNodeType() {
    return this._deploymentNodeType;
  }
  
  public boolean isLocal() {
    return this._isLocal;
  }
}
