package com.vmware.ph.phservice.common.cdf.internal;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.common.cdf.FileSystemPayloadUploader;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyControlledPayloadUploaderWrapper implements PayloadUploader {
  private static final Log _log = LogFactory.getLog(PropertyControlledPayloadUploaderWrapper.class);
  
  protected final ConfigurationService _configurationService;
  
  protected final String _propNameForPayloadLocation;
  
  protected final ExecutorService _executorService;
  
  protected final PayloadUploader _fallbackPayloadUploader;
  
  public PropertyControlledPayloadUploaderWrapper(ConfigurationService configurationService, String payloadFilePathProperty, ExecutorService executorService, PayloadUploader fallbackPayloadUploader) {
    this._configurationService = configurationService;
    this._propNameForPayloadLocation = payloadFilePathProperty;
    this._executorService = executorService;
    this._fallbackPayloadUploader = fallbackPayloadUploader;
  }
  
  public boolean isEnabled() {
    PayloadUploader payloadUploader = getPayloadUploader();
    return (payloadUploader != null && payloadUploader.isEnabled());
  }
  
  public Future<?> uploadPayload(Payload payload, PayloadEnvelope envelope, String uploadId) {
    PayloadUploader payloadUploader = getPayloadUploader();
    Future<?> result = null;
    if (payloadUploader != null) {
      result = payloadUploader.uploadPayload(payload, envelope, uploadId);
    } else if (_log.isWarnEnabled()) {
      _log.warn("There is no configured payload uploader.Upload will not be performed.");
    } 
    return result;
  }
  
  protected PayloadUploader getPayloadUploader() {
    String payloadLocation = this._configurationService.getProperty(this._propNameForPayloadLocation);
    PayloadUploader payloadUploader = null;
    if (!StringUtils.isBlank(payloadLocation)) {
      payloadUploader = new FileSystemPayloadUploader(payloadLocation, this._executorService, false);
    } else {
      payloadUploader = this._fallbackPayloadUploader;
    } 
    return payloadUploader;
  }
}
