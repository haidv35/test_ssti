package com.vmware.ph.phservice.common.internal;

import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompositeFuture<T> implements Future<List<T>> {
  private static final Log _log = LogFactory.getLog(CompositeFuture.class);
  
  private final Iterable<Future<T>> _futures;
  
  private boolean _isDone;
  
  private boolean _isCancelled;
  
  private final List<T> _results;
  
  public CompositeFuture(Collection<Future<T>> futures) {
    this._futures = futures;
    this._results = new ArrayList<>(futures.size());
  }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (this._isDone || this._isCancelled)
      return false; 
    Iterator<Future<T>> futuresIterator = this._futures.iterator();
    while (futuresIterator.hasNext()) {
      Future<T> future = futuresIterator.next();
      future.cancel(mayInterruptIfRunning);
    } 
    this._isDone = true;
    this._isCancelled = true;
    return this._isCancelled;
  }
  
  public boolean isCancelled() {
    return this._isCancelled;
  }
  
  public boolean isDone() {
    return this._isDone;
  }
  
  public List<T> get() throws InterruptedException, ExecutionException {
    if (this._isDone)
      return this._results; 
    Iterator<Future<T>> futuresIterator = this._futures.iterator();
    while (futuresIterator.hasNext()) {
      try {
        Future<T> future = futuresIterator.next();
        this._results.add(future.get());
      } catch (CancellationException|ExecutionException|InterruptedException e) {
        ExceptionsContextManager.store(e);
        if (_log.isDebugEnabled())
          _log.debug("Future execution failed.", e); 
      } finally {
        if (this._isCancelled)
          throw new CancellationException("Interrupted while waiting for futures to complete."); 
      } 
    } 
    this._isDone = true;
    return this._results;
  }
  
  public List<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (this._isDone)
      return this._results; 
    Iterator<Future<T>> futuresIterator = this._futures.iterator();
    long timeoutInMillis = unit.toMillis(timeout);
    while (futuresIterator.hasNext() && timeoutInMillis > 0L) {
      long startMillis = System.currentTimeMillis();
      try {
        Future<T> future = futuresIterator.next();
        this._results.add(future.get(timeoutInMillis, TimeUnit.MILLISECONDS));
      } catch (CancellationException|ExecutionException|InterruptedException e) {
        if (_log.isDebugEnabled())
          _log.debug("Future execution failed", e); 
      } finally {
        if (this._isCancelled)
          throw new CancellationException("Interrupted while waiting for futures to complete."); 
        long endMillis = System.currentTimeMillis();
        long executionTime = endMillis - startMillis;
        timeoutInMillis -= executionTime;
      } 
    } 
    this._isDone = true;
    if (timeoutInMillis <= 0L)
      throw new TimeoutException(
          String.format("Not all Future-s completed execution for the specified time of %d %s.", new Object[] { Long.valueOf(timeout), unit.name() })); 
    return this._results;
  }
}
