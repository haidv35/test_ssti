package com.vmware.ph.phservice.cloud.dataapp.service;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class DefaultDataApp implements DataApp {
  private static final Log _log = LogFactory.getLog(DefaultDataApp.class);
  
  public ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, ManifestSpec manifestSpec, String objectId) {
    ManifestInfo manifest = null;
    if (manifestSpec != null) {
      if (objectId != null)
        manifest = getManifestInfo(collectorId, collectorInstanceId, manifestSpec

            
            .getDataType(), objectId, manifestSpec
            
            .getVersionDataType(), objectId); 
      if (manifest == null) {
        _log.info("No object-specific manifest found so we will retrieve the global manifest.");
        String manifestResourceId = (manifestSpec.getResourceId() != null) ? manifestSpec.getResourceId() : collectorInstanceId;
        manifest = getManifestInfo(collectorId, manifestResourceId, manifestSpec

            
            .getDataType(), manifestSpec
            .getObjectId(), manifestSpec
            .getVersionDataType(), manifestSpec
            .getVersionObjectId());
      } 
    } 
    if (manifest == null) {
      _log.info(
          String.format("Could not find a manifest in DAP with the given manifest spec for %s. Will fetch the manifest from the manifest API.", new Object[] { collectorId }));
      manifest = getManifestInfo(collectorId, collectorInstanceId);
    } 
    return manifest;
  }
  
  protected abstract ManifestInfo getManifestInfo(String paramString1, String paramString2);
  
  protected abstract ManifestInfo getManifestInfo(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6);
}
