package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;

public class SsoFixedTokenProviderImpl implements SsoTokenProvider {
  private final SsoTokenProvider.TokenKeyPair _fixedTokenKeyPair;
  
  public SsoFixedTokenProviderImpl(SsoTokenProvider.TokenKeyPair fixedTokenKeyPair) {
    this._fixedTokenKeyPair = fixedTokenKeyPair;
  }
  
  public SsoTokenProvider.TokenKeyPair getToken() throws SsoTokenProviderException {
    return this._fixedTokenKeyPair;
  }
}
