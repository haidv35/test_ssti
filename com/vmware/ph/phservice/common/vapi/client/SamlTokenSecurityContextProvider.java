package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ExecutionContext;

public class SamlTokenSecurityContextProvider implements SecurityContextProvider {
  private final SsoTokenProvider _ssoTokenProvider;
  
  public SamlTokenSecurityContextProvider(SsoTokenProvider ssoTokenProvider) {
    this._ssoTokenProvider = ssoTokenProvider;
  }
  
  public ExecutionContext.SecurityContext getSecurityContext(StubFactory stubFactory) throws Exception {
    SsoTokenProvider.TokenKeyPair tokenKeyPair = this._ssoTokenProvider.getToken();
    return SecurityContextFactory.createSamlSecurityContext(tokenKeyPair.token, tokenKeyPair.key);
  }
  
  public void deleteSecurityContext(StubFactory stubFactory, ExecutionContext.SecurityContext securityContext) throws Exception {}
}
