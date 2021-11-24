package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.cis.data.internal.util.QueryMarker;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.ExecutionContext;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.ErrorValue;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionAwareApiProvider implements ApiProvider {
  private static Logger _logger = LoggerFactory.getLogger(SessionAwareApiProvider.class);
  
  private final ApiProvider _api;
  
  private final VapiSession _session;
  
  private final URI _providerURI;
  
  public SessionAwareApiProvider(ApiProvider api, VapiSession session, URI providerURI) {
    assert api != null;
    assert session != null;
    assert providerURI != null;
    this._api = api;
    this._session = session;
    this._providerURI = providerURI;
  }
  
  public void invoke(String serviceId, String operationId, DataValue input, ExecutionContext ctx, AsyncHandle<MethodResult> asyncHandle) {
    char[] sessionId = this._session.get();
    ExecutionContext.ApplicationData appData = createApplicationData(ctx);
    ExecutionContext ctxWithSession = VapiInvocationSecurity.ctxWithSessionId(sessionId, appData);
    AsyncHandleFuture asyncHandleFuture = new AsyncHandleFuture();
    if (_logger.isTraceEnabled()) {
      Object[] logObjects = { serviceId, operationId, this._providerURI };
      _logger.trace("Sending vAPI operation {}.{} to endpoint: {}", logObjects);
    } 
    this._api.invoke(serviceId, operationId, input, ctxWithSession, asyncHandleFuture);
    MethodResult result = getResult(asyncHandleFuture, asyncHandle);
    if (result == null)
      return; 
    if (isUnauthenticated(result)) {
      Object[] logObjects = { serviceId, operationId, this._providerURI };
      _logger.info("vAPI operation {}.{} failed with Unauthenticated to {}. Will re-login and retry.", logObjects);
      char[] newSessionId = this._session.renew(sessionId);
      ExecutionContext ctxWithNewSession = VapiInvocationSecurity.ctxWithSessionId(newSessionId, appData);
      asyncHandleFuture = new AsyncHandleFuture();
      this._api.invoke(serviceId, operationId, input, ctxWithNewSession, asyncHandleFuture);
      result = getResult(asyncHandleFuture, asyncHandle);
      if (result == null)
        return; 
    } 
    asyncHandle.setResult(result);
  }
  
  private MethodResult getResult(AsyncHandleFuture asyncHandleFuture, AsyncHandle<MethodResult> asyncHandle) {
    while (true) {
      try {
        return asyncHandleFuture.get();
      } catch (RuntimeException e) {
        asyncHandle.setError(e);
        return null;
      } catch (InterruptedException interruptedException) {}
    } 
  }
  
  private static boolean isUnauthenticated(MethodResult result) {
    assert result != null;
    ErrorValue errorValue = result.getError();
    return (errorValue != null && "com.vmware.vapi.std.errors.unauthenticated".equals(errorValue.getName()));
  }
  
  private static class AsyncHandleFuture extends AsyncHandle<MethodResult> {
    private final Lock lock = new ReentrantLock();
    
    private final Condition set = this.lock.newCondition();
    
    private boolean completed = false;
    
    private MethodResult result;
    
    private RuntimeException error;
    
    public MethodResult get() throws InterruptedException, RuntimeException {
      this.lock.lock();
      try {
        while (!this.completed)
          this.set.await(); 
        if (this.error == null)
          return this.result; 
        throw this.error;
      } finally {
        this.lock.unlock();
      } 
    }
    
    public void setResult(MethodResult result) {
      this.lock.lock();
      try {
        this.result = result;
        this.completed = true;
        this.set.signalAll();
      } finally {
        this.lock.unlock();
      } 
    }
    
    public void setError(RuntimeException error) {
      this.lock.lock();
      try {
        this.error = error;
        this.completed = true;
        this.set.signalAll();
      } finally {
        this.lock.unlock();
      } 
    }
    
    public void updateProgress(DataValue progress) {}
    
    private AsyncHandleFuture() {}
  }
  
  private ExecutionContext.ApplicationData createApplicationData(ExecutionContext ctx) {
    Map<String, String> properties;
    ExecutionContext.ApplicationData appData = ctx.retrieveApplicationData();
    String queryId = QueryMarker.getQueryId();
    if (queryId == null)
      return appData; 
    if (appData == null) {
      properties = new HashMap<>();
    } else {
      properties = new HashMap<>(appData.getAllProperties());
    } 
    properties.put("opId", queryId);
    return new ExecutionContext.ApplicationData(properties);
  }
}
