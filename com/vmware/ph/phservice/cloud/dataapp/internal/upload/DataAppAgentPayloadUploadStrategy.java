package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;
import java.io.UnsupportedEncodingException;

public class DataAppAgentPayloadUploadStrategy implements AgentPayloadUploadStrategy {
  private final DataApp _dataApp;
  
  public DataAppAgentPayloadUploadStrategy(DataApp dataApp) {
    this._dataApp = dataApp;
  }
  
  public void upload(AgentJsonUploadRequest jsonUploadRequest) throws AgentPayloadUploadException {
    PluginData pluginData;
    PluginTypeContext pluginContext = jsonUploadRequest.getPluginContext();
    try {
      pluginData = new PluginData(pluginContext.getPluginType(), jsonUploadRequest.getJson().getBytes("UTF-8"), false, pluginContext.getDataType(), jsonUploadRequest.getObjectId());
    } catch (UnsupportedEncodingException e) {
      throw new AgentPayloadUploadException("Unable to convert payload to byte[]", e);
    } 
    try {
      this._dataApp.uploadData(jsonUploadRequest
          .getCollectorId(), jsonUploadRequest
          .getCollectorInstanceId(), jsonUploadRequest
          .getCollectionId(), jsonUploadRequest
          .getDeploymentSecret(), pluginData);
    } catch (Exception e) {
      throw new AgentPayloadUploadException("Data App upload failed.", e);
    } 
  }
}
