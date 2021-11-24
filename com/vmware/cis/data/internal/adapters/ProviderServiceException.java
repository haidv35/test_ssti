package com.vmware.cis.data.internal.adapters;

public class ProviderServiceException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public ProviderServiceException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ProviderServiceException(String message) {
    super(message);
  }
  
  public ProviderServiceException(Throwable cause) {
    super(cause);
  }
}
