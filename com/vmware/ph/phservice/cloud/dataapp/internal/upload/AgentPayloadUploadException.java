package com.vmware.ph.phservice.cloud.dataapp.internal.upload;

public class AgentPayloadUploadException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public AgentPayloadUploadException(String message, Exception e) {
    super(message, e);
  }
  
  public AgentPayloadUploadException(String message) {
    super(message);
  }
}
