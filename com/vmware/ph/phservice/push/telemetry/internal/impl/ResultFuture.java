package com.vmware.ph.phservice.push.telemetry.internal.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResultFuture<T> implements Future<T> {
  private final T _result;
  
  public ResultFuture(T result) {
    this._result = result;
  }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    throw new UnsupportedOperationException("The cancel operation is not supported.");
  }
  
  public boolean isCancelled() {
    return false;
  }
  
  public boolean isDone() {
    return true;
  }
  
  public T get() throws InterruptedException, ExecutionException {
    return this._result;
  }
  
  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return this._result;
  }
}
