package com.vmware.ph.phservice.cloud.dataapp;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;

public class DataAppAgentCreateInfo {
  private final Builder<ManifestSpec> _manifestSpecBuilder;
  
  public DataAppAgentCreateInfo(Builder<ManifestSpec> manifestSpecBuilder) {
    this._manifestSpecBuilder = manifestSpecBuilder;
  }
  
  public Builder<ManifestSpec> getManifestSpecBuilder() {
    return this._manifestSpecBuilder;
  }
}
