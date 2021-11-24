package com.vmware.ph.phservice.provider.vcenter.license.client;

public class LicenseClientException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public LicenseClientException(Throwable cause) {
    super(cause);
  }
  
  public LicenseClientException(String message) {
    super(message);
  }
  
  public LicenseClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
