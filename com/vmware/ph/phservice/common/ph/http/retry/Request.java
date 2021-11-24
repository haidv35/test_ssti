package com.vmware.ph.phservice.common.ph.http.retry;

import java.io.IOException;
import java.util.Objects;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

public class Request<T> {
  private final HttpUriRequest _request;
  
  private final String _requestTag;
  
  private final ResponseHandler<T> _successResponseHandler;
  
  private final ResponseHandler<T> _errorResponseHandler;
  
  private final BeforeRetryHandler _beforeRetryHandler;
  
  private Request(HttpUriRequest request, String requestTag, ResponseHandler<T> successResponseHandler, ResponseHandler<T> errorResponseHandler, BeforeRetryHandler beforeRetryHandler) {
    this._request = request;
    this._requestTag = requestTag;
    this._successResponseHandler = successResponseHandler;
    this._errorResponseHandler = errorResponseHandler;
    this._beforeRetryHandler = beforeRetryHandler;
  }
  
  public HttpUriRequest getRequest() {
    return this._request;
  }
  
  public String getRequestTag() {
    return this._requestTag;
  }
  
  public ResponseHandler<T> getSuccessResponseHandler() {
    return this._successResponseHandler;
  }
  
  public ResponseHandler<T> getErrorResponseHandler() {
    return this._errorResponseHandler;
  }
  
  public BeforeRetryHandler getBeforeRetryHandler() {
    return this._beforeRetryHandler;
  }
  
  public static class Builder<T> {
    private final ResponseHandler<T> NO_OP_RESPONSE_HANDLER = response -> null;
    
    private HttpUriRequest _request;
    
    private String _requestTag;
    
    private ResponseHandler<T> _successResponseHandler = this.NO_OP_RESPONSE_HANDLER;
    
    private ResponseHandler<T> _errorResponseHandler = this.NO_OP_RESPONSE_HANDLER;
    
    private BeforeRetryHandler _beforeRetryHandler = retryIntervalMillis -> {
      
      };
    
    public Builder<T> forRequest(HttpUriRequest request) {
      this._request = request;
      return this;
    }
    
    public Builder<T> forTag(String requestTag) {
      this._requestTag = requestTag;
      return this;
    }
    
    public Builder<T> onSuccess(ResponseHandler<T> successResponseHandler) {
      this._successResponseHandler = successResponseHandler;
      return this;
    }
    
    public Builder<T> onError(ResponseHandler<T> errorResponseHandler) {
      this._errorResponseHandler = errorResponseHandler;
      return this;
    }
    
    public Builder<T> beforeRetry(BeforeRetryHandler beforeRetryHandler) {
      this._beforeRetryHandler = beforeRetryHandler;
      return this;
    }
    
    public Request<T> build() {
      return new Request<>(
          Objects.<HttpUriRequest>requireNonNull(this._request, "A valid HTTP request must be provided."), 
          Objects.<String>requireNonNull(this._requestTag, "A valid request tag must be provided."), 
          Objects.<ResponseHandler>requireNonNull(this._successResponseHandler, "A valid successful response handler must be provided."), 
          Objects.<ResponseHandler>requireNonNull(this._errorResponseHandler, "A valid error response handler must be provided."), 
          Objects.<BeforeRetryHandler>requireNonNull(this._beforeRetryHandler, "A valid before retry handler must be provided."));
    }
  }
}
