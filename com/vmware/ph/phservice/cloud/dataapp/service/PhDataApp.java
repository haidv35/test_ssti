package com.vmware.ph.phservice.cloud.dataapp.service;

import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.common.cdf.internal.dataapp.PluginResultUtil;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.common.ph.PhDapClient;
import com.vmware.ph.phservice.common.ph.PhDapClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class PhDataApp extends DefaultDataApp {
  private static final Log _log = LogFactory.getLog(PhDataApp.class);
  
  private static final String DEFAULT_MANIFEST_VERSION = "1.0";
  
  private static final String MANIFEST_VERSION_PROPERTY_NAME = "version";
  
  private final PhDapClientFactory _phDapClientFactory;
  
  private final ManifestContentProvider _manifestContentProvider;
  
  public PhDataApp(PhDapClientFactory phDapClientFactory, ManifestContentProvider manifestContentProvider) {
    this._phDapClientFactory = phDapClientFactory;
    this._manifestContentProvider = manifestContentProvider;
  }
  
  public void uploadData(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, PluginData data) {
    try (PhDapClient phDapClient = this._phDapClientFactory.create()) {
      phDapClient.sendPluginData(collectorId, collectorInstanceId, collectionId, null, deploymentSecret, data);
    } 
  }
  
  public String getResult(String collectorId, String collectorInstanceId, String deploymentSecret, String dataType, String objectId, Long sinceTimestamp) {
    try (PhDapClient phDapClient = this._phDapClientFactory.create()) {
      return phDapClient.queryResult(collectorId, collectorInstanceId, deploymentSecret, dataType, objectId, sinceTimestamp);
    } 
  }
  
  protected ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId) {
    String content = null;
    try {
      content = this._manifestContentProvider.getManifestContent(collectorId, collectorInstanceId);
    } catch (com.vmware.ph.phservice.common.manifest.ManifestContentProvider.ManifestException e) {
      _log.warn("Could not retrieve the data app manifest.", e);
    } 
    return new ManifestInfo(content, "1.0");
  }
  
  protected ManifestInfo getManifestInfo(String collectorId, String collectorInstanceId, String manifestDataType, String manifestObjectId, String versionDataType, String versionObjectId) {
    String content = getResultValue(collectorId, collectorInstanceId, manifestDataType, manifestObjectId);
    _log.info(
        String.format("Retrieved manifest content for collector [%s] and instance [%s] with data type [%s] and objectId [%s]: %s", new Object[] { collectorId, collectorInstanceId, manifestDataType, manifestObjectId, content }));
    if (content == null)
      return null; 
    String version = null;
    if (StringUtils.isNotBlank(versionDataType) || 
      StringUtils.isNotBlank(versionObjectId))
      version = getResultValue(collectorId, collectorInstanceId, versionDataType, versionObjectId); 
    if (version != null) {
      JSONObject versionJson = new JSONObject(version);
      version = versionJson.optString("version");
    } else {
      version = "1.0";
    } 
    return new ManifestInfo(content, version);
  }
  
  private String getResultValue(String collectorId, String collectorInstanceId, String dataType, String objectId) {
    Iterable<PluginResult> pluginResults = null;
    try {
      String result = getResult(collectorId, collectorInstanceId, null, dataType, objectId, null);
      pluginResults = PluginResultUtil.parsePluginResults(collectorId, collectorInstanceId, result);
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      if (_log.isWarnEnabled())
        _log.warn("Unable to get result value: " + e.getMessage()); 
    } 
    String value = null;
    if (pluginResults != null && pluginResults.iterator().hasNext()) {
      PluginResult pluginResult = pluginResults.iterator().next();
      value = pluginResult.getContent().toString();
    } 
    return value;
  }
}
