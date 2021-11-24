package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.StsClient;
import com.vmware.ph.phservice.common.cis.internal.sso.StsClientFactory;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.vim.sso.client.SamlToken;

public class SsoBearerTokenProviderImpl implements SsoTokenProvider {
  private final StsClientFactory _stsClientFactory;
  
  private final String _username;
  
  private final char[] _password;
  
  private final int _tokenLifetimeSecs = 600;
  
  public SsoBearerTokenProviderImpl(StsClientFactory stsClientFactory, String username, char[] password) {
    this._stsClientFactory = stsClientFactory;
    this._username = username;
    this._password = password;
  }
  
  public SsoTokenProvider.TokenKeyPair getToken() throws SsoTokenProviderException {
    try {
      StsClient stsClient = this._stsClientFactory.createStsClient();
      SamlToken token = stsClient.acquireBearerToken(this._username, this._password, 600L);
      SsoTokenProvider.TokenKeyPair result = new SsoTokenProvider.TokenKeyPair();
      result.token = token;
      return result;
    } catch (Exception e) {
      throw new SsoTokenProviderException(e);
    } 
  }
}
