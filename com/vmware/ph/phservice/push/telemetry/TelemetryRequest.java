package com.vmware.ph.phservice.push.telemetry;

public class TelemetryRequest {
  private final String _version;
  
  private final String _collectorId;
  
  private final String _collectorIntanceId;
  
  private final byte[] _data;
  
  private final boolean _isCompressed;
  
  public TelemetryRequest(String version, String collectorId, String collectorInstanceId, byte[] data, boolean isCompressed) {
    this._version = version;
    this._collectorId = collectorId;
    this._collectorIntanceId = collectorInstanceId;
    this._data = data;
    this._isCompressed = isCompressed;
  }
  
  public String getVersion() {
    return this._version;
  }
  
  public String getCollectorId() {
    return this._collectorId;
  }
  
  public String getCollectorIntanceId() {
    return this._collectorIntanceId;
  }
  
  public byte[] getData() {
    return this._data;
  }
  
  public boolean isCompressed() {
    return this._isCompressed;
  }
}
