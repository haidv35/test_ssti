package com.vmware.cis.data.internal.adapters.util.vapi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RenewableVapiSession implements VapiSession {
  private static Logger _logger = LoggerFactory.getLogger(RenewableVapiSession.class);
  
  private final VapiSessionSource _authenticator;
  
  private final AtomicReference<Future<char[]>> _sessionIdFuture;
  
  public RenewableVapiSession(VapiSessionSource authenticator) {
    assert authenticator != null;
    this._authenticator = authenticator;
    this._sessionIdFuture = new AtomicReference<>(authenticator.createSession());
  }
  
  public char[] get() {
    Future<char[]> future = this._sessionIdFuture.get();
    validateIsOpen(future);
    return getSessionId(future);
  }
  
  public char[] renew(char[] expired) {
    assert expired != null;
    Future<char[]> currentFuture = this._sessionIdFuture.get();
    validateIsOpen(currentFuture);
    char[] current = getSessionId(this._sessionIdFuture);
    if (current != expired)
      return current; 
    Future<char[]> freshFuture = this._authenticator.createSession();
    char[] fresh = getSessionId(freshFuture);
    boolean changed = this._sessionIdFuture.compareAndSet(currentFuture, freshFuture);
    if (changed) {
      _logger.debug("Renewed session from {}", this._authenticator);
      return fresh;
    } 
    _logger.debug("Destroying redundant new session from {} because somebody renewed the session before us", this._authenticator);
    silentLogout(fresh);
    return get();
  }
  
  public void logout() {
    Future<char[]> session = this._sessionIdFuture.getAndSet(null);
    if (session == null)
      return; 
    silentLogout(getSessionId(session));
  }
  
  private static char[] getSessionId(AtomicReference<Future<char[]>> sessionIdRef) {
    return getSessionId(sessionIdRef.get());
  }
  
  private static char[] getSessionId(Future<char[]> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof RuntimeException)
        throw (RuntimeException)e.getCause(); 
      throw new RuntimeException(e.getMessage(), e.getCause());
    } 
  }
  
  private void silentLogout(char[] session) {
    try {
      this._authenticator.deleteSession(session);
    } catch (RuntimeException ex) {
      _logger.debug("Error while logging out session", ex);
    } 
  }
  
  private void validateIsOpen(Future<char[]> future) {
    if (future == null)
      throw new IllegalStateException("Session is closed"); 
  }
}
