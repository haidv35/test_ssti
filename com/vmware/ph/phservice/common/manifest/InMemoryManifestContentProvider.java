package com.vmware.ph.phservice.common.manifest;

public class InMemoryManifestContentProvider implements ManifestContentProvider {
  private final String _manifestContent;
  
  public InMemoryManifestContentProvider(String manifestContent) {
    this._manifestContent = manifestContent;
  }
  
  public boolean isEnabled() {
    return true;
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    return this._manifestContent;
  }
}
