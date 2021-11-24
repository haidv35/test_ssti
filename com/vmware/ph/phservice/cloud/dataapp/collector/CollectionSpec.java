package com.vmware.ph.phservice.cloud.dataapp.collector;

import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.internal.obfuscation.Obfuscator;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;

public class CollectionSpec {
  private final ManifestContentProvider _manifestContentProvider;
  
  private final PayloadUploader _payloadUploader;
  
  private final Obfuscator _obfuscator;
  
  private final Object _queryContextData;
  
  private final String _objectId;
  
  private final String _pluginType;
  
  private final String _dataType;
  
  private final boolean _shouldSignalCollectionCompleted;
  
  public CollectionSpec(ManifestContentProvider manifestContentProvider, PayloadUploader payloadUploader, Obfuscator obfuscator, Object queryContextData, String objectId, String pluginType, String dataType, boolean shouldSignalCollectionCompleted) {
    this._manifestContentProvider = manifestContentProvider;
    this._payloadUploader = payloadUploader;
    this._obfuscator = obfuscator;
    this._queryContextData = queryContextData;
    this._objectId = objectId;
    this._pluginType = pluginType;
    this._dataType = dataType;
    this._shouldSignalCollectionCompleted = shouldSignalCollectionCompleted;
  }
  
  public String getObjectId() {
    return this._objectId;
  }
  
  public String getPluginType() {
    return this._pluginType;
  }
  
  public String getDataType() {
    return this._dataType;
  }
  
  public ManifestContentProvider getManifestContentProvider() {
    return this._manifestContentProvider;
  }
  
  public PayloadUploader getPayloadUploader() {
    return this._payloadUploader;
  }
  
  public Obfuscator getObfuscator() {
    return this._obfuscator;
  }
  
  public Object getQueryContextData() {
    return this._queryContextData;
  }
  
  public boolean getSignalCollectionCompleted() {
    return this._shouldSignalCollectionCompleted;
  }
}
