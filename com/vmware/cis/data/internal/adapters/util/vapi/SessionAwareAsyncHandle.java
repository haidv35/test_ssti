package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.ExecutionContext;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.ErrorValue;
import com.vmware.vapi.util.async.DecoratorAsyncHandle;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SessionAwareAsyncHandle extends DecoratorAsyncHandle<MethodResult> {
  private static Logger _logger = LoggerFactory.getLogger(SessionAwareAsyncHandle.class);
  
  private final VapiSession _session;
  
  private final VapiInvokeCommand _invocation;
  
  private final ExecutionContext.ApplicationData _appData;
  
  private final char[] _sessionId;
  
  private final URI _providerURI;
  
  SessionAwareAsyncHandle(AsyncHandle<MethodResult> asyncHandle, VapiSession session, VapiInvokeCommand invocation, ExecutionContext.ApplicationData appData, char[] sessionId, URI providerURI) {
    super(asyncHandle);
    assert session != null;
    assert invocation != null;
    assert appData != null;
    assert sessionId != null;
    assert providerURI != null;
    this._session = session;
    this._invocation = invocation;
    this._appData = appData;
    this._sessionId = sessionId;
    this._providerURI = providerURI;
  }
  
  public void setResult(MethodResult result) {
    assert result != null;
    try {
      setResultThrowing(result);
    } catch (RuntimeException ex) {
      setError(ex);
    } 
  }
  
  private void setResultThrowing(MethodResult result) {
    assert result != null;
    if (isUnauthenticated(result)) {
      Object[] logObjects = { this._invocation.getServiceId(), this._invocation.getOperationId(), this._providerURI };
      _logger.info("Operation {}.{} failed with Unauthenticated to {}. Will re-login and retry.", logObjects);
      char[] newSessionId = this._session.renew(this._sessionId);
      this._invocation.execute(VapiInvocationSecurity.ctxWithSessionId(newSessionId, this._appData), this.decorated);
    } else {
      super.setResult(result);
    } 
  }
  
  private static boolean isUnauthenticated(MethodResult result) {
    assert result != null;
    ErrorValue errorValue = result.getError();
    return (errorValue != null && "com.vmware.vapi.std.errors.unauthenticated".equals(errorValue.getName()));
  }
}
