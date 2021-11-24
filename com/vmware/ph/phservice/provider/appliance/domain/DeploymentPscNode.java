package com.vmware.ph.phservice.provider.appliance.domain;

public class DeploymentPscNode {
  private String _id;
  
  private String _siteId;
  
  private String _domainId;
  
  private boolean _isEmbeddedDeployment;
  
  private DeploymentInfo.ServiceInfo _serviceInfo;
  
  public DeploymentPscNode(String id, String siteId, String domainId, boolean isEmbeddedDeployment, DeploymentInfo.ServiceInfo serviceInfo) {
    this._id = id;
    this._siteId = siteId;
    this._domainId = domainId;
    this._isEmbeddedDeployment = isEmbeddedDeployment;
    this._serviceInfo = serviceInfo;
  }
  
  public String getId() {
    return this._id;
  }
  
  public String getSiteId() {
    return this._siteId;
  }
  
  public String getDomainId() {
    return this._domainId;
  }
  
  public boolean getIsEmbeddedDeployment() {
    return this._isEmbeddedDeployment;
  }
  
  public DeploymentInfo.ServiceInfo getServiceInfo() {
    return this._serviceInfo;
  }
  
  public String toString() {
    return String.format("DeploymentPscNode [id=%s, siteId=%s, domainId=%s, isEmbeddedDeployment=%s, serviceInfo=%s]", new Object[] { getId(), 
          getSiteId(), 
          getDomainId(), 
          Boolean.valueOf(getIsEmbeddedDeployment()), 
          getServiceInfo().toString() });
  }
}
