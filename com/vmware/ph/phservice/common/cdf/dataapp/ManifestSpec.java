package com.vmware.ph.phservice.common.cdf.dataapp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ManifestSpec {
  private final String _resourceId;
  
  private final String _dataType;
  
  private final String _objectId;
  
  private final String _versionDataType;
  
  private final String _versionObjectId;
  
  public ManifestSpec(String resourceId, String dataType, String objectId, String versionDataType, String versionObjectId) {
    this._resourceId = resourceId;
    this._dataType = dataType;
    this._objectId = objectId;
    this._versionDataType = versionDataType;
    this._versionObjectId = versionObjectId;
  }
  
  public String getDataType() {
    return this._dataType;
  }
  
  public String getObjectId() {
    return this._objectId;
  }
  
  public String getVersionDataType() {
    return this._versionDataType;
  }
  
  public String getVersionObjectId() {
    return this._versionObjectId;
  }
  
  public String getResourceId() {
    return this._resourceId;
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
  
  public String toString() {
    StringBuilder builder = (new StringBuilder()).append("ManifestSpec [_dataType=").append(this._dataType).append(", _objectId=").append(this._objectId).append(", _resourceId=").append(this._resourceId).append(", _versionDataType=").append(this._versionDataType).append(", _versionObjectId=").append(this._versionObjectId).append("]");
    return builder.toString();
  }
}
