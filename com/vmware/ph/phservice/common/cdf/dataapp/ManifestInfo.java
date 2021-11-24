package com.vmware.ph.phservice.common.cdf.dataapp;

public class ManifestInfo {
  private final String _content;
  
  private final String _version;
  
  public ManifestInfo(String content, String version) {
    this._content = content;
    this._version = version;
  }
  
  public String getContent() {
    return this._content;
  }
  
  public String getVersion() {
    return this._version;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + ((this._content == null) ? 0 : this._content.hashCode());
    result = 31 * result + ((this._version == null) ? 0 : this._version.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (!(obj instanceof ManifestInfo))
      return false; 
    ManifestInfo other = (ManifestInfo)obj;
    if (this._content == null) {
      if (other._content != null)
        return false; 
    } else if (!this._content.equals(other._content)) {
      return false;
    } 
    if (this._version == null) {
      if (other._version != null)
        return false; 
    } else if (!this._version.equals(other._version)) {
      return false;
    } 
    return true;
  }
}
