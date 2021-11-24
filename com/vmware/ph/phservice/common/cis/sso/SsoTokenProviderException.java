package com.vmware.ph.phservice.common.cis.sso;

public class SsoTokenProviderException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public SsoTokenProviderException(Exception e) {
    super(e);
  }
}
