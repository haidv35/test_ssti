package com.vmware.ph.phservice.ceip.internal;

public class CeipApiException extends RuntimeException {
  private static final long serialVersionUID = 6999358336073942139L;
  
  public CeipApiException(String msg) {
    super(msg);
  }
  
  public CeipApiException(Throwable e) {
    super(e);
  }
  
  public CeipApiException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
