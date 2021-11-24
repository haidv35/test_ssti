package com.vmware.ph.phservice.ceip;

public class ConsentException extends Exception {
  private static final long serialVersionUID = 6999358376073942139L;
  
  public ConsentException(String msg) {
    super(msg);
  }
  
  public ConsentException(Throwable e) {
    super(e);
  }
  
  public ConsentException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
