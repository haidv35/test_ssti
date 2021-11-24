package com.vmware.ph.phservice.common.cdf.internal;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import java.util.List;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompositePayloadUploader implements PayloadUploader {
  private static final Log _log = LogFactory.getLog(CompositePayloadUploader.class);
  
  private List<PayloadUploader> _payloadUploaders;
  
  public CompositePayloadUploader(List<PayloadUploader> payloadUploaders) {
    this._payloadUploaders = payloadUploaders;
  }
  
  public boolean isEnabled() {
    return (getFirstEnabledPayloadUploader() != null);
  }
  
  public Future<?> uploadPayload(Payload payload, PayloadEnvelope envelope, String uploadId) {
    Future<?> result = null;
    PayloadUploader payloadUploader = getFirstEnabledPayloadUploader();
    if (payloadUploader != null) {
      result = payloadUploader.uploadPayload(payload, envelope, uploadId);
    } else if (_log.isWarnEnabled()) {
      _log.warn("There is no enabled payload uploader.Upload will not be performed.");
    } 
    return result;
  }
  
  private PayloadUploader getFirstEnabledPayloadUploader() {
    PayloadUploader firstEnabledPayloadUploader = null;
    for (PayloadUploader payloadUploader : this._payloadUploaders) {
      if (payloadUploader != null && payloadUploader.isEnabled()) {
        firstEnabledPayloadUploader = payloadUploader;
        break;
      } 
    } 
    return firstEnabledPayloadUploader;
  }
}
