package com.vmware.ph.phservice.provider.appliance.domain;

public class DeploymentDomain {
  private String _id;
  
  private int _siteCount;
  
  private int _pscNodeCount;
  
  public DeploymentDomain(String id, int siteCount, int pscNodeCount) {
    this._id = id;
    this._siteCount = siteCount;
    this._pscNodeCount = pscNodeCount;
  }
  
  public String getId() {
    return this._id;
  }
  
  public int getSiteCount() {
    return this._siteCount;
  }
  
  public int getPscNodeCount() {
    return this._pscNodeCount;
  }
  
  public String toString() {
    return String.format("DeploymentDomain [id=%s, siteCount=%s, pscNodeCount=%s]", new Object[] { getId(), 
          Integer.valueOf(getSiteCount()), 
          Integer.valueOf(getPscNodeCount()) });
  }
}
