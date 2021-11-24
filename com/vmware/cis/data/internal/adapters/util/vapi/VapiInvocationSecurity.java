package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vapi.bindings.client.InvocationConfig;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ExecutionContext;

public final class VapiInvocationSecurity {
  public static InvocationConfig cfgWithToken(AuthenticationTokenSource credentials) {
    assert credentials != null;
    return new InvocationConfig(ctxWithToken(credentials, null));
  }
  
  public static ExecutionContext ctxWithToken(AuthenticationTokenSource credentials, ExecutionContext.ApplicationData appData) {
    assert credentials != null;
    ExecutionContext.SecurityContext securityWithToken = SecurityContextFactory.createSamlSecurityContext(credentials.getAuthenticationToken(), credentials
        .getConfirmationKey());
    return new ExecutionContext(appData, securityWithToken);
  }
  
  public static InvocationConfig cfgWithSessionId(char[] sessionId) {
    assert sessionId != null;
    return new InvocationConfig(ctxWithSessionId(sessionId, null));
  }
  
  public static ExecutionContext ctxWithSessionId(char[] sessionId, ExecutionContext.ApplicationData appData) {
    assert sessionId != null;
    ExecutionContext.SecurityContext securityWithSession = SecurityContextFactory.createSessionSecurityContext(sessionId);
    return new ExecutionContext(appData, securityWithSession);
  }
}
