package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.phservice.common.internal.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyControlledAgentPayloadUploadStrategy implements AgentPayloadUploadStrategy {
  private static final Log _log = LogFactory.getLog(PropertyControlledAgentPayloadUploadStrategy.class);
  
  private final ConfigurationService _configurationService;
  
  private final String _propNameForPayloadLocation;
  
  public PropertyControlledAgentPayloadUploadStrategy(ConfigurationService configurationService, String payloadFilePathProperty) {
    this._configurationService = configurationService;
    this._propNameForPayloadLocation = payloadFilePathProperty;
  }
  
  public void upload(AgentJsonUploadRequest jsonUploadRequest) throws AgentPayloadUploadException {
    AgentPayloadUploadStrategy uploadStrategy = getPayloadUploadStrategy();
    if (uploadStrategy != null) {
      uploadStrategy.upload(jsonUploadRequest);
    } else {
      _log.warn("There is no configured payload repository.Upload will not be performed.");
    } 
  }
  
  private AgentPayloadUploadStrategy getPayloadUploadStrategy() {
    String targetPayloadLocationPathName = this._configurationService.getProperty(this._propNameForPayloadLocation);
    AgentPayloadUploadStrategy uploadStrategy = null;
    if (StringUtils.isNotBlank(targetPayloadLocationPathName))
      uploadStrategy = new FileSystemAgentPayloadUploadStrategy(targetPayloadLocationPathName, true); 
    return uploadStrategy;
  }
}
