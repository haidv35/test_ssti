package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.internal.adapters.vmomi.VmomiSession;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

final class RenewableVcVmomiSession implements VmomiSession {
  private final VmodlContext _vmodlContext;
  
  private final URI _vmomiUri;
  
  private final HttpConfiguration _vlsiHttpConfig;
  
  private final AuthenticationTokenSource _tokenSource;
  
  private final AtomicReference<VcVmomiSession> _currentSession;
  
  private final Object _loginMonitor = new Object();
  
  RenewableVcVmomiSession(VmodlContext vmodlContext, URI vmomiUri, HttpConfiguration vlsiHttpConfig, AuthenticationTokenSource tokenSource) {
    assert vmodlContext != null;
    assert vmomiUri != null;
    assert vlsiHttpConfig != null;
    assert tokenSource != null;
    this._vmodlContext = vmodlContext;
    this._vmomiUri = vmomiUri;
    this._vlsiHttpConfig = vlsiHttpConfig;
    this._tokenSource = tokenSource;
    this._currentSession = new AtomicReference<>(createSession());
  }
  
  public String getSessionCookie() {
    VcVmomiSession currentSession = this._currentSession.get();
    if (currentSession == null)
      throw new IllegalStateException("Session is closed"); 
    return currentSession.getSessionCookie();
  }
  
  public String renewSessionCookie(String expiredSessionCookie) {
    synchronized (this._loginMonitor) {
      VcVmomiSession currentSession = this._currentSession.get();
      if (currentSession == null)
        throw new IllegalStateException("Session is closed"); 
      String currentCookie = currentSession.getSessionCookie();
      if (expiredSessionCookie.equals(currentCookie)) {
        VcVmomiSession candidate = createSession();
        this._currentSession.set(candidate);
        currentSession.logout();
        currentCookie = candidate.getSessionCookie();
      } 
      return currentCookie;
    } 
  }
  
  public void logout() {
    synchronized (this._loginMonitor) {
      VcVmomiSession currentSession = this._currentSession.getAndSet(null);
      if (currentSession != null)
        currentSession.logout(); 
    } 
  }
  
  private VcVmomiSession createSession() {
    return VcVmomiSession.createSession(this._vmodlContext, this._vmomiUri, this._vlsiHttpConfig, this._tokenSource);
  }
}
