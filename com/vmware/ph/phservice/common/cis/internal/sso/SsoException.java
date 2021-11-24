package com.vmware.ph.phservice.common.cis.internal.sso;

public class SsoException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public SsoException(String message) {
    super(message);
  }
  
  public SsoException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public SsoException(Throwable cause) {
    super(cause);
  }
}
