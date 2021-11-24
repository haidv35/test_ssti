package com.vmware.ph.phservice.collector.internal.manifest;

public class InvalidManifestException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public InvalidManifestException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public InvalidManifestException(String message) {
    super(message);
  }
}
