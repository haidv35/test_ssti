package com.vmware.ph.phservice.common.cdf;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.common.internal.CompositeFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class MultiplexingPayloadUploader implements PayloadUploader {
  private final List<PayloadUploader> _payloadUploaders;
  
  public MultiplexingPayloadUploader(List<PayloadUploader> payloadUploaders) {
    this._payloadUploaders = payloadUploaders;
  }
  
  public boolean isEnabled() {
    for (PayloadUploader payloadUploader : this._payloadUploaders) {
      if (payloadUploader.isEnabled())
        return true; 
    } 
    return false;
  }
  
  public Future<?> uploadPayload(Payload payload, PayloadEnvelope envelope, String uploadId) {
    List<Future<Object>> payloadUploaderResults = new ArrayList<>(this._payloadUploaders.size());
    for (PayloadUploader payloadUploader : this._payloadUploaders) {
      if (payloadUploader.isEnabled()) {
        Future<Object> uploadResult = (Future)payloadUploader.uploadPayload(payload, envelope, uploadId);
        payloadUploaderResults.add(uploadResult);
      } 
    } 
    return (Future<?>)new CompositeFuture(payloadUploaderResults);
  }
}
