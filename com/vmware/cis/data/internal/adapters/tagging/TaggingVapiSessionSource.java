package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.internal.adapters.util.vapi.VapiInvocationSecurity;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiOsgiAwareStubFactory;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiSessionSource;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.tagging.sessions.SessionManager;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.AsyncCallbackFuture;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.std.errors.Unauthenticated;
import java.util.concurrent.Future;

public final class TaggingVapiSessionSource implements VapiSessionSource {
  private final SessionManager _sessionManager;
  
  private final AuthenticationTokenSource _credentials;
  
  TaggingVapiSessionSource(ApiProvider api, AuthenticationTokenSource credentials) {
    assert api != null;
    assert credentials != null;
    this
      ._sessionManager = (new VapiOsgiAwareStubFactory(api)).<SessionManager>createStub(SessionManager.class);
    this._credentials = credentials;
  }
  
  public Future<char[]> createSession() {
    AsyncCallbackFuture<char[]> asyncCallbackFuture = new AsyncCallbackFuture();
    this._sessionManager.login((AsyncCallback)asyncCallbackFuture, VapiInvocationSecurity.cfgWithToken(this._credentials));
    return (Future<char[]>)asyncCallbackFuture;
  }
  
  public void deleteSession(char[] sessionId) {
    if (sessionId == null)
      return; 
    try {
      this._sessionManager.logout(VapiInvocationSecurity.cfgWithSessionId(sessionId));
    } catch (Unauthenticated unauthenticated) {}
  }
}
