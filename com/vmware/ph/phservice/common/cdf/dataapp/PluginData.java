package com.vmware.ph.phservice.common.cdf.dataapp;

public class PluginData {
  private final String _pluginType;
  
  private byte[] _data;
  
  private final boolean _isCompressed;
  
  private final String _dataType;
  
  private final String _objectId;
  
  public PluginData(String pluginType, byte[] data, boolean isCompressed, String dataType, String objectId) {
    this._pluginType = pluginType;
    this._data = data;
    this._isCompressed = isCompressed;
    this._dataType = (dataType != null) ? dataType : (pluginType + "_data");
    this._objectId = objectId;
  }
  
  public String getPluginType() {
    return this._pluginType;
  }
  
  public byte[] getData() {
    return this._data;
  }
  
  public boolean isCompressed() {
    return this._isCompressed;
  }
  
  public String getDataType() {
    return this._dataType;
  }
  
  public String getObjectId() {
    return this._objectId;
  }
}
