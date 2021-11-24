package com.vmware.ph.phservice.common.internal.manifest;

import com.vmware.ph.phservice.common.internal.ConfigurationService;
import com.vmware.ph.phservice.common.manifest.FileSystemManifestContentProvider;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyControlledManifestContentProviderWrapper implements ManifestContentProvider {
  private static final Log _log = LogFactory.getLog(PropertyControlledManifestContentProviderWrapper.class);
  
  private final ConfigurationService _configurationService;
  
  private final String _propNameForManifestFilePath;
  
  private final ManifestContentProvider _fallbackManifestContentProvider;
  
  public PropertyControlledManifestContentProviderWrapper(ConfigurationService configurationService, String propNameForManifestFilePath, ManifestContentProvider fallbackManifestContentProvider) {
    this._configurationService = configurationService;
    this._propNameForManifestFilePath = propNameForManifestFilePath;
    this._fallbackManifestContentProvider = fallbackManifestContentProvider;
  }
  
  public boolean isEnabled() {
    ManifestContentProvider manifestContentProvider = getManifestContentProvider();
    return (manifestContentProvider != null && manifestContentProvider.isEnabled());
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    ManifestContentProvider manifestContentProvider = getManifestContentProvider();
    String manifest = null;
    if (manifestContentProvider != null)
      manifest = manifestContentProvider.getManifestContent(collectorId, collectorInstanceId); 
    return manifest;
  }
  
  private ManifestContentProvider getManifestContentProvider() {
    String manifestFilePathName = this._configurationService.getProperty(this._propNameForManifestFilePath);
    boolean isManifestLocationPropertySet = (manifestFilePathName != null && !manifestFilePathName.trim().isEmpty());
    if (_log.isDebugEnabled())
      _log.debug("Reading the manifest from file: " + isManifestLocationPropertySet); 
    ManifestContentProvider manifestContentProvider = null;
    if (isManifestLocationPropertySet) {
      manifestContentProvider = new FileSystemManifestContentProvider(manifestFilePathName);
    } else {
      manifestContentProvider = this._fallbackManifestContentProvider;
    } 
    return manifestContentProvider;
  }
}
