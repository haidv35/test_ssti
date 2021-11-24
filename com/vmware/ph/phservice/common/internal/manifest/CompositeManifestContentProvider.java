package com.vmware.ph.phservice.common.internal.manifest;

import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import java.util.List;

public class CompositeManifestContentProvider implements ManifestContentProvider {
  private List<ManifestContentProvider> _manifestContentProviders;
  
  public CompositeManifestContentProvider(List<ManifestContentProvider> manifestContentProviders) {
    this._manifestContentProviders = manifestContentProviders;
  }
  
  public boolean isEnabled() {
    return (getFirstEnabledManifestContentProvider() != null);
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    ManifestContentProvider manifestContentProvider = getFirstEnabledManifestContentProvider();
    if (manifestContentProvider != null)
      return manifestContentProvider.getManifestContent(collectorId, collectorInstanceId); 
    return null;
  }
  
  private ManifestContentProvider getFirstEnabledManifestContentProvider() {
    ManifestContentProvider firstEnabledManifestContentProvider = null;
    for (ManifestContentProvider manifestContentProvider : this._manifestContentProviders) {
      if (manifestContentProvider != null && manifestContentProvider.isEnabled()) {
        firstEnabledManifestContentProvider = manifestContentProvider;
        break;
      } 
    } 
    return firstEnabledManifestContentProvider;
  }
}
