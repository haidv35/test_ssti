package com.vmware.ph.phservice.cloud.dataapp.service;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;

public class LocalDataApp implements DataApp {
  public ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, ManifestSpec manifestSpec, String objectId) {
    return null;
  }
  
  public void uploadData(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, PluginData data) {}
  
  public String getResult(String collectorId, String collectorInstanceId, String deploymentSecret, String dataType, String objectId, Long sinceTimestamp) {
    return null;
  }
}
