package com.vmware.ph.phservice.common.cdf.internal.dataapp;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import org.apache.commons.lang3.StringUtils;

public class PropertyControlledManifestSpecBuilder implements Builder<ManifestSpec> {
  private final ConfigurationService _configurationService;
  
  private final Schema _schema;
  
  public PropertyControlledManifestSpecBuilder(ConfigurationService configurationService, Schema schema) {
    this._configurationService = configurationService;
    this._schema = schema;
  }
  
  public ManifestSpec build() {
    String manifestResourceId = this._configurationService.getProperty(this._schema._propNameForResourceId);
    String manifestObjecId = this._configurationService.getProperty(this._schema._propNameForObjectId);
    String manifestDataType = this._configurationService.getProperty(this._schema._propNameForDataType);
    String manifestVersionObjecId = this._configurationService.getProperty(this._schema._propNameForVersionObjectId);
    String manifestVersionDataType = this._configurationService.getProperty(this._schema._propNameForVersionDataType);
    if (StringUtils.isBlank(manifestResourceId) && 
      StringUtils.isBlank(manifestObjecId) && 
      StringUtils.isBlank(manifestDataType))
      return null; 
    return new ManifestSpec(manifestResourceId, manifestDataType, manifestObjecId, manifestVersionDataType, manifestVersionObjecId);
  }
  
  public static class Schema {
    private final String _propNameForResourceId;
    
    private final String _propNameForObjectId;
    
    private final String _propNameForDataType;
    
    private final String _propNameForVersionObjectId;
    
    private final String _propNameForVersionDataType;
    
    public Schema(String propNameForResourceId, String propNameForObjectId, String propNameForDataType, String propNameForVersionObjectId, String propNameForVersionDataType) {
      this._propNameForResourceId = propNameForResourceId;
      this._propNameForObjectId = propNameForObjectId;
      this._propNameForDataType = propNameForDataType;
      this._propNameForVersionObjectId = propNameForVersionObjectId;
      this._propNameForVersionDataType = propNameForVersionDataType;
    }
    
    public String getPropNameForManifestResourceId() {
      return this._propNameForResourceId;
    }
    
    public String getPropNameForManifestObjectId() {
      return this._propNameForObjectId;
    }
    
    public String getPropNameForManifestDataType() {
      return this._propNameForDataType;
    }
    
    public String getPropNameForManifestVersionObjectId() {
      return this._propNameForVersionObjectId;
    }
    
    public String getPropNameForManifestVersionDataType() {
      return this._propNameForVersionDataType;
    }
  }
}
