package com.vmware.ph.phservice.common.ph.http.retry;

public class Response<T> {
  private final int _statusCode;
  
  private final T _result;
  
  public Response(int statusCode, T result) {
    this._statusCode = statusCode;
    this._result = result;
  }
  
  public int getStatusCode() {
    return this._statusCode;
  }
  
  public T getResult() {
    return this._result;
  }
}
