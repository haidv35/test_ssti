package com.vmware.ph.phservice.common.internal.exceptions;

public class StatusCodeException extends RuntimeException {
  private int _statusCode;
  
  private static final long serialVersionUID = 1L;
  
  public StatusCodeException(int statusCode, Throwable cause) {
    super(cause);
    this._statusCode = statusCode;
  }
  
  public StatusCodeException(int statusCode, String message) {
    super(message);
    this._statusCode = statusCode;
  }
  
  public StatusCodeException(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this._statusCode = statusCode;
  }
  
  public int getStatusCode() {
    return this._statusCode;
  }
}
