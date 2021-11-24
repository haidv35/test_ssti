package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import java.util.List;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MultiplexingAgentPayloadUploadStrategy implements AgentPayloadUploadStrategy {
  private static final Log _log = LogFactory.getLog(MultiplexingAgentPayloadUploadStrategy.class);
  
  private final List<AgentPayloadUploadStrategy> _uploadStrategies;
  
  public MultiplexingAgentPayloadUploadStrategy(List<AgentPayloadUploadStrategy> uploadStrategies) {
    this._uploadStrategies = Objects.<List<AgentPayloadUploadStrategy>>requireNonNull(uploadStrategies);
  }
  
  public void upload(AgentJsonUploadRequest jsonUploadRequest) {
    for (AgentPayloadUploadStrategy uploadStrategy : this._uploadStrategies) {
      try {
        uploadStrategy.upload(jsonUploadRequest);
      } catch (AgentPayloadUploadException e) {
        ExceptionsContextManager.store(e);
        if (_log.isWarnEnabled())
          _log.warn(String.format("Upload of JSON payload for strategy %s failed. Continuing with other strategies if any.", new Object[] { uploadStrategy
                  
                  .getClass().getSimpleName() }), e); 
      } 
    } 
  }
}
