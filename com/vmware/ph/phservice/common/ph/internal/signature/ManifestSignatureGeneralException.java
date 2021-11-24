package com.vmware.ph.phservice.common.ph.internal.signature;

public class ManifestSignatureGeneralException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public ManifestSignatureGeneralException(String message) {
    super(message);
  }
  
  public ManifestSignatureGeneralException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ManifestSignatureGeneralException(Throwable cause) {
    super(cause);
  }
}
