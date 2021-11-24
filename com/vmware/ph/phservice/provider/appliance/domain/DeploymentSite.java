package com.vmware.ph.phservice.provider.appliance.domain;

public class DeploymentSite {
  private String _id;
  
  private String _domainId;
  
  private int _pscNodeCount;
  
  public DeploymentSite(String id, String domainId, int pscNodeCount) {
    this._id = id;
    this._domainId = domainId;
    this._pscNodeCount = pscNodeCount;
  }
  
  public String getId() {
    return this._id;
  }
  
  public String getDomainId() {
    return this._domainId;
  }
  
  public int getPscNodeCount() {
    return this._pscNodeCount;
  }
  
  public String toString() {
    return String.format("DeploymentSite [id=%s, domainId=%s, pscNodeCount=%s]", new Object[] { getId(), 
          getDomainId(), 
          Integer.valueOf(getPscNodeCount()) });
  }
}
