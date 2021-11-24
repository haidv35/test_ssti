package com.vmware.ph.phservice.common.cdf.dataapp;

public class ManifestInfoId {
  private final String _collectorId;
  
  private final String _collectorInstanceId;
  
  private final String _contentDataType;
  
  private final String _contentObjectId;
  
  private final String _versionDataType;
  
  private final String _versionObjectId;
  
  public ManifestInfoId(String collectorId, String collectorInstanceId) {
    this(collectorId, collectorInstanceId, null, null, null, null);
  }
  
  public ManifestInfoId(String collectorId, String collectorInstanceId, String contentDataType, String contentObjectId, String versionDataType, String versionObjectId) {
    this._collectorId = collectorId;
    this._collectorInstanceId = collectorInstanceId;
    this._contentDataType = contentDataType;
    this._contentObjectId = contentObjectId;
    this._versionDataType = versionDataType;
    this._versionObjectId = versionObjectId;
  }
  
  public String toString() {
    return "ManifestInfoId [_collectorId=" + this._collectorId + ", _collectorInstanceId=" + this._collectorInstanceId + ", _contentDataType=" + this._contentDataType + ", _contentObjectId=" + this._contentObjectId + ", _versionDataType=" + this._versionDataType + ", _versionObjectId=" + this._versionObjectId + "]";
  }
  
  public String getContentObjectId() {
    return this._contentObjectId;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + ((this._collectorId == null) ? 0 : this._collectorId.hashCode());
    result = 31 * result + ((this._collectorInstanceId == null) ? 0 : this._collectorInstanceId.hashCode());
    result = 31 * result + ((this._contentDataType == null) ? 0 : this._contentDataType.hashCode());
    result = 31 * result + ((this._contentObjectId == null) ? 0 : this._contentObjectId.hashCode());
    result = 31 * result + ((this._versionDataType == null) ? 0 : this._versionDataType.hashCode());
    result = 31 * result + ((this._versionObjectId == null) ? 0 : this._versionObjectId.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (!(obj instanceof ManifestInfoId))
      return false; 
    ManifestInfoId other = (ManifestInfoId)obj;
    if (this._collectorId == null) {
      if (other._collectorId != null)
        return false; 
    } else if (!this._collectorId.equals(other._collectorId)) {
      return false;
    } 
    if (this._collectorInstanceId == null) {
      if (other._collectorInstanceId != null)
        return false; 
    } else if (!this._collectorInstanceId.equals(other._collectorInstanceId)) {
      return false;
    } 
    if (this._contentDataType == null) {
      if (other._contentDataType != null)
        return false; 
    } else if (!this._contentDataType.equals(other._contentDataType)) {
      return false;
    } 
    if (this._contentObjectId == null) {
      if (other._contentObjectId != null)
        return false; 
    } else if (!this._contentObjectId.equals(other._contentObjectId)) {
      return false;
    } 
    if (this._versionDataType == null) {
      if (other._versionDataType != null)
        return false; 
    } else if (!this._versionDataType.equals(other._versionDataType)) {
      return false;
    } 
    if (this._versionObjectId == null) {
      if (other._versionObjectId != null)
        return false; 
    } else if (!this._versionObjectId.equals(other._versionObjectId)) {
      return false;
    } 
    return true;
  }
}
