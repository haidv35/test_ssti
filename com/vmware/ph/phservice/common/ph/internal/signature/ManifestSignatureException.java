package com.vmware.ph.phservice.common.ph.internal.signature;

public class ManifestSignatureException extends Exception {
  private static final long serialVersionUID = 1L;
  
  private final Reason _reason;
  
  public ManifestSignatureException(String message) {
    this(message, Reason.UNSPECIFIED);
  }
  
  public ManifestSignatureException(String message, Reason reason) {
    super(message);
    this._reason = reason;
  }
  
  public ManifestSignatureException(String message, Throwable cause) {
    this(message, cause, Reason.UNSPECIFIED);
  }
  
  public ManifestSignatureException(String message, Throwable cause, Reason reason) {
    super(message, cause);
    this._reason = reason;
  }
  
  public Reason getReason() {
    return this._reason;
  }
  
  public enum Reason {
    UNSPECIFIED, CERT_PATH_NOT_VERIFIED, MISSING_TIMESTAMP, CLIENT_METADATA_NOT_VERIFIED, UNSIGNED_ENTRIES, MISSING_ENTRIES, INCORRECT_CN, IMPROPER_FORMAT;
  }
}
