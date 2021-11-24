package com.vmware.ph.phservice.common.cdf.dataapp;

import java.util.Date;

public class PluginResult {
  private final String _objectId;
  
  private final Object _content;
  
  private final Date _timestamp;
  
  public PluginResult(String objectId, Object content, Date timestamp) {
    this._objectId = objectId;
    this._content = content;
    this._timestamp = timestamp;
  }
  
  public String getObjectId() {
    return this._objectId;
  }
  
  public Object getContent() {
    return this._content;
  }
  
  public Date getTimestamp() {
    return this._timestamp;
  }
}
