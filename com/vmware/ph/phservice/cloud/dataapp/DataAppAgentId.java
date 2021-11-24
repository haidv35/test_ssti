package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.common.internal.IdFormatUtil;
import java.util.Objects;

public final class DataAppAgentId {
  private final String _collectorId;
  
  private final String _collectorInstanceId;
  
  private final String _deploymentSecret;
  
  private final String _pluginType;
  
  public DataAppAgentId(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType) throws DataAppAgentManager.IncorrectFormat {
    if (!IdFormatUtil.isValidCollectorId(collectorId) || 
      !IdFormatUtil.isValidCollectorInstanceId(collectorInstanceId))
      throw new DataAppAgentManager.IncorrectFormat(); 
    this._collectorId = collectorId;
    this._collectorInstanceId = collectorInstanceId;
    this._deploymentSecret = deploymentSecret;
    this._pluginType = pluginType;
  }
  
  public String getCollectorId() {
    return this._collectorId;
  }
  
  public String getCollectorInstanceId() {
    return this._collectorInstanceId;
  }
  
  public String getDeploymentSecret() {
    return this._deploymentSecret;
  }
  
  public String getPluginType() {
    return this._pluginType;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    DataAppAgentId that = (DataAppAgentId)o;
    return (Objects.equals(this._collectorId, that._collectorId) && 
      Objects.equals(this._collectorInstanceId, that._collectorInstanceId) && 
      Objects.equals(this._deploymentSecret, that._deploymentSecret) && 
      Objects.equals(this._pluginType, that._pluginType));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this._collectorId, this._collectorInstanceId, this._deploymentSecret, this._pluginType });
  }
  
  public String toString() {
    return "DataAppAgentId [_collectorId=" + this._collectorId + ", _collectorInstanceId=" + this._collectorInstanceId + ", _pluginType=" + this._pluginType + "]";
  }
}
