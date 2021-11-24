package com.vmware.ph.phservice.cloud.dataapp.service;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;

public interface DataApp {
  ManifestInfo getManifestInfo(String paramString1, String paramString2, ManifestSpec paramManifestSpec, String paramString3);
  
  void uploadData(String paramString1, String paramString2, String paramString3, String paramString4, PluginData paramPluginData);
  
  String getResult(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, Long paramLong);
}
