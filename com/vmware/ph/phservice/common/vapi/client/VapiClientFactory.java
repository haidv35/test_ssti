package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import java.net.URI;
import java.security.KeyStore;

public class VapiClientFactory {
  private final KeyStore _trustStore;
  
  public VapiClientFactory(KeyStore trustStore) {
    this._trustStore = trustStore;
  }
  
  public VapiClient createClient(URI vapiUri, SsoTokenProvider ssoTokenProvider) {
    return createClient(vapiUri, ssoTokenProvider, false);
  }
  
  public VapiClient createClient(URI vapiUri, SsoTokenProvider ssoTokenProvider, boolean withSessionAuthentication) {
    SecurityContextProvider securityContextProvider;
    if (withSessionAuthentication) {
      securityContextProvider = new SessionSecurityContextProvider(ssoTokenProvider);
    } else {
      securityContextProvider = new SamlTokenSecurityContextProvider(ssoTokenProvider);
    } 
    return new VapiClient(vapiUri, this._trustStore, securityContextProvider);
  }
  
  public VapiClient createClient(URI vapiUri, String osUser, char[] osPassword) {
    SecurityContextProvider securityContextProvider = new UserPassSecurityContextProvider(osUser, osPassword);
    return new VapiClient(vapiUri, this._trustStore, securityContextProvider);
  }
}
