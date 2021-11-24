package com.vmware.ph.phservice.cloud.dataapp.service;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;
import com.vmware.ph.phservice.common.ph.PhEnvironmentProvider;
import com.vmware.ph.upload.service.UploadServiceBuilder;

public class PhEnvironmentDataApp extends DefaultDataApp {
  private final PhEnvironmentProvider _phEnvironmentProvider;
  
  private final DefaultDataApp _prodDataApp;
  
  private final DefaultDataApp _stageDataApp;
  
  public PhEnvironmentDataApp(PhEnvironmentProvider phEnvironmentProvider, DefaultDataApp prodPhDataApp, DefaultDataApp stagePhDataApp) {
    this._phEnvironmentProvider = phEnvironmentProvider;
    this._prodDataApp = prodPhDataApp;
    this._stageDataApp = stagePhDataApp;
  }
  
  public ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, ManifestSpec manifestSpec, String objectId) {
    return super.getManifestInfo(collectorId, collectorInstanceId, manifestSpec, objectId);
  }
  
  public void uploadData(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, PluginData data) {
    DataApp dataApp = getDataApp();
    dataApp.uploadData(collectorId, collectorInstanceId, collectionId, deploymentSecret, data);
  }
  
  public String getResult(String collectorId, String collectorInstanceId, String deploymentSecret, String dataType, String objectId, Long sinceTimestamp) {
    DataApp dataApp = getDataApp();
    String result = dataApp.getResult(collectorId, collectorInstanceId, deploymentSecret, dataType, objectId, sinceTimestamp);
    return result;
  }
  
  protected ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId) {
    DefaultDataApp dataApp = getDataApp();
    ManifestInfo manifestInfo = dataApp.getManifestInfo(collectorId, collectorInstanceId);
    return manifestInfo;
  }
  
  protected ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, String manifestDataType, String manifestObjectId, String versionDataType, String versionObjectId) {
    DefaultDataApp dataApp = getDataApp();
    ManifestInfo manifestInfo = dataApp.getManifestInfo(collectorId, collectorInstanceId, manifestDataType, manifestObjectId, versionDataType, versionObjectId);
    return manifestInfo;
  }
  
  private DefaultDataApp getDataApp() {
    UploadServiceBuilder.Environment environment = this._phEnvironmentProvider.getEnvironment();
    if (environment == UploadServiceBuilder.Environment.PRODUCTION)
      return this._prodDataApp; 
    return this._stageDataApp;
  }
}
