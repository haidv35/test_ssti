package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.common.cdf.internal.PayloadFileUtil;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

public class FileSystemAgentPayloadUploadStrategy implements AgentPayloadUploadStrategy {
  private final String _outputDirectoryPathName;
  
  private final boolean _shouldArchivePayloads;
  
  public FileSystemAgentPayloadUploadStrategy(String outputDirectoryPathName, boolean shouldArchivePayloads) {
    this._outputDirectoryPathName = outputDirectoryPathName;
    this._shouldArchivePayloads = shouldArchivePayloads;
  }
  
  public void upload(AgentJsonUploadRequest jsonUploadRequest) throws AgentPayloadUploadException {
    if (StringUtils.isBlank(this._outputDirectoryPathName))
      throw new AgentPayloadUploadException(FileSystemAgentPayloadUploadStrategy.class + " is not configured properly, it is not able to process specified payload."); 
    try {
      String jsonFileName = computeJsonFileName(jsonUploadRequest
          .getObjectId(), jsonUploadRequest
          .getPluginContext());
      PayloadFileUtil.writeJsonToFile(this._outputDirectoryPathName, jsonUploadRequest
          
          .getJson(), jsonFileName, this._shouldArchivePayloads);
    } catch (IOException e) {
      throw new AgentPayloadUploadException(
          String.format("Json upload for collector [%s] failed.", new Object[] { jsonUploadRequest.getCollectorId() }), e);
    } 
  }
  
  private static String computeJsonFileName(String objectId, PluginTypeContext pluginTypeContext) {
    return computePayloadFileName("", objectId, pluginTypeContext);
  }
  
  private static String computePayloadFileName(String fileName, String objectId, PluginTypeContext pluginTypeContext) {
    String pluginType = pluginTypeContext.getPluginType();
    String dataType = pluginTypeContext.getDataType();
    if (StringUtils.isBlank(dataType) && StringUtils.isBlank(pluginType))
      return null; 
    String fullFileName = fileName + ".json.gz";
    String dataTypeFilenamePart = !StringUtils.isBlank(dataType) ? dataType : String.format("%s_data", new Object[] { pluginType });
    fullFileName = dataTypeFilenamePart + fullFileName;
    if (!StringUtils.isBlank(objectId))
      fullFileName = objectId + "-" + fullFileName; 
    return fullFileName;
  }
}
