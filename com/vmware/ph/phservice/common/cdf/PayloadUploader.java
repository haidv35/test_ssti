package com.vmware.ph.phservice.common.cdf;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import java.util.concurrent.Future;

public interface PayloadUploader {
  boolean isEnabled();
  
  Future<?> uploadPayload(Payload paramPayload, PayloadEnvelope paramPayloadEnvelope, String paramString);
  
  public static class PayloadUploadException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public PayloadUploadException(String message) {
      super(message);
    }
    
    public PayloadUploadException(Exception e) {
      super(e);
    }
    
    public PayloadUploadException(String message, Exception e) {
      super(message, e);
    }
  }
}
