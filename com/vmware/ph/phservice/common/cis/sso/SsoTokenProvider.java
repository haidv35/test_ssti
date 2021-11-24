package com.vmware.ph.phservice.common.cis.sso;

import com.vmware.vim.sso.client.SamlToken;
import java.security.PrivateKey;

public interface SsoTokenProvider {
  TokenKeyPair getToken() throws SsoTokenProviderException;
  
  public static class TokenKeyPair {
    public SamlToken token;
    
    public PrivateKey key;
  }
}
