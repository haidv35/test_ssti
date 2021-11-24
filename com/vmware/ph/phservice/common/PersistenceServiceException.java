package com.vmware.ph.phservice.common;

public class PersistenceServiceException extends Exception {
  private static final long serialVersionUID = 4056673420415787129L;
  
  public PersistenceServiceException() {}
  
  public PersistenceServiceException(String message) {
    super(message);
  }
  
  public PersistenceServiceException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public PersistenceServiceException(Throwable cause) {
    super(cause);
  }
}
