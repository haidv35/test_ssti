package com.vmware.ph.phservice.push.telemetry;

public class CollectorAgent {
  private final String _collectorId;
  
  private final String _collectorInstanceId;
  
  public CollectorAgent(String collectorId, String collectorInstanceId) {
    this._collectorId = collectorId;
    this._collectorInstanceId = collectorInstanceId;
  }
  
  public String getCollectorId() {
    return this._collectorId;
  }
  
  public String getCollectorInstanceId() {
    return this._collectorInstanceId;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof CollectorAgent))
      return false; 
    CollectorAgent other = (CollectorAgent)obj;
    boolean result = true;
    if (!this._collectorId.equals(other._collectorId)) {
      result = false;
    } else if (this._collectorInstanceId == null) {
      if (other._collectorInstanceId != null)
        result = false; 
    } else if (!this._collectorInstanceId.equals(other._collectorInstanceId)) {
      result = false;
    } 
    return result;
  }
  
  public int hashCode() {
    int hash = this._collectorId.hashCode();
    if (this._collectorInstanceId != null)
      hash = hash * 31 + this._collectorInstanceId.hashCode(); 
    return hash;
  }
  
  public String toString() {
    String str = "CollectorAgent: {collectorId:" + this._collectorId + ", collectorInstanceId:" + this._collectorInstanceId + "}";
    return str;
  }
}
