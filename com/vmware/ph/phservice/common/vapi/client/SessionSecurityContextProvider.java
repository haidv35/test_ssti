package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.cis.Session;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubConfigurationBase;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.bindings.client.InvocationConfig;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ExecutionContext;

public class SessionSecurityContextProvider implements SecurityContextProvider {
  private final SsoTokenProvider _ssoTokenProvider;
  
  public SessionSecurityContextProvider(SsoTokenProvider ssoTokenProvider) {
    this._ssoTokenProvider = ssoTokenProvider;
  }
  
  public ExecutionContext.SecurityContext getSecurityContext(StubFactory stubFactory) throws Exception {
    Session sessionService = (Session)stubFactory.createStub(Session.class, (StubConfigurationBase)
        
        getSessionServiceStubConfiguration());
    char[] sessionId = sessionService.create();
    ExecutionContext.SecurityContext sessionSecurityContext = SecurityContextFactory.createSessionSecurityContext(sessionId);
    return sessionSecurityContext;
  }
  
  public void deleteSecurityContext(StubFactory stubFactory, ExecutionContext.SecurityContext securityContext) throws Exception {
    Session sessionService = (Session)stubFactory.createStub(Session.class, (StubConfigurationBase)
        
        getSessionServiceStubConfiguration());
    ExecutionContext sessionServiceExecutionContext = new ExecutionContext(securityContext);
    InvocationConfig sessionServiceInvocationConfig = new InvocationConfig(sessionServiceExecutionContext);
    sessionService.delete(sessionServiceInvocationConfig);
  }
  
  private StubConfiguration getSessionServiceStubConfiguration() throws Exception {
    SsoTokenProvider.TokenKeyPair tokenKeyPair = this._ssoTokenProvider.getToken();
    ExecutionContext.SecurityContext samlSecurityContext = SecurityContextFactory.createSamlSecurityContext(tokenKeyPair.token, tokenKeyPair.key);
    StubConfiguration sessionServiceStubConfiguration = new StubConfiguration(samlSecurityContext);
    return sessionServiceStubConfiguration;
  }
}
