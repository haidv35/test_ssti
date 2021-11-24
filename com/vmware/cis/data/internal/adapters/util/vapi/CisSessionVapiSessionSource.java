package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.cis.Session;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.AsyncCallbackFuture;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.std.errors.Unauthenticated;
import java.util.concurrent.Future;

public final class CisSessionVapiSessionSource implements VapiSessionSource {
  private final Session _sessionManager;
  
  private final AuthenticationTokenSource _credentials;
  
  public CisSessionVapiSessionSource(ApiProvider api, AuthenticationTokenSource credentials) {
    assert api != null;
    assert credentials != null;
    this
      ._sessionManager = (new VapiOsgiAwareStubFactory(api)).<Session>createStub(Session.class);
    this._credentials = credentials;
  }
  
  public Future<char[]> createSession() {
    AsyncCallbackFuture<char[]> asyncCallbackFuture = new AsyncCallbackFuture();
    this._sessionManager.create((AsyncCallback)asyncCallbackFuture, VapiInvocationSecurity.cfgWithToken(this._credentials));
    return (Future<char[]>)asyncCallbackFuture;
  }
  
  public void deleteSession(char[] sessionId) {
    if (sessionId == null)
      return; 
    try {
      this._sessionManager.delete(VapiInvocationSecurity.cfgWithSessionId(sessionId));
    } catch (Unauthenticated unauthenticated) {}
  }
}
