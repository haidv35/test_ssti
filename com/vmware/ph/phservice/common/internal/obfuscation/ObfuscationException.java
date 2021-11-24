package com.vmware.ph.phservice.common.internal.obfuscation;

public class ObfuscationException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public ObfuscationException(String message) {
    super(message);
  }
  
  public ObfuscationException(Throwable e) {
    super(e);
  }
  
  public ObfuscationException(String message, Throwable cause) {
    super(message, cause);
  }
}
