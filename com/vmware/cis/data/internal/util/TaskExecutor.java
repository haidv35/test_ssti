package com.vmware.cis.data.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskExecutor {
  public enum ErrorHandlingPolicy {
    STRICT, LENIENT;
  }
  
  private static final Logger _logger = LoggerFactory.getLogger(TaskExecutor.class);
  
  private static final String ERROR_EXECUTION_CANCELLED = "Data Service execution was cancelled.";
  
  private final ExecutorService _executor;
  
  private final ErrorHandlingPolicy _errorHandling;
  
  public TaskExecutor(ExecutorService executor, ErrorHandlingPolicy errorHandling) {
    this._executor = executor;
    this._errorHandling = errorHandling;
  }
  
  public <T> List<T> invokeTasks(List<Callable<T>> tasks) {
    if (tasks.size() == 0)
      return Collections.emptyList(); 
    List<ExecutionInfo<T>> execInfoList = submitForExecution(tasks);
    assert tasks.size() == execInfoList.size();
    List<T> results = new ArrayList<>(tasks.size());
    List<Throwable> failedTaskErrors = new ArrayList<>();
    for (ExecutionInfo<T> execPair : execInfoList) {
      T result;
      Callable<T> task = execPair.getTask();
      Future<T> future = execPair.getFuture();
      try {
        result = future.get();
      } catch (InterruptedException e) {
        cancelAll(execInfoList);
        Thread.currentThread().interrupt();
        throw new RuntimeException("Data Service execution was cancelled.", e);
      } catch (ExecutionException e) {
        Throwable actualError = unwrapExecutionException(e);
        applyErrorHandlingPolicy(task, actualError, execInfoList);
        failedTaskErrors.add(actualError);
        continue;
      } 
      results.add(result);
    } 
    assert this._errorHandling != ErrorHandlingPolicy.STRICT || failedTaskErrors.isEmpty();
    RuntimeException error = toAggregatedError(failedTaskErrors);
    if (error != null && results.isEmpty())
      throw error; 
    return results;
  }
  
  private <T> List<ExecutionInfo<T>> submitForExecution(List<Callable<T>> tasks) {
    List<ExecutionInfo<T>> execInfoList = new ArrayList<>(tasks.size());
    for (int i = 0; i < tasks.size(); i++) {
      if (Thread.currentThread().isInterrupted())
        throw new RuntimeException("Data Service execution was cancelled."); 
      Callable<T> task = tasks.get(i);
      if (this._executor == null || i == tasks.size() - 1) {
        executeDirectly(task, execInfoList);
      } else {
        try {
          long startTime = System.currentTimeMillis();
          Future<T> future = this._executor.submit(task);
          ExecutionInfo<T> execInfo = new ExecutionInfo<>(future, task, startTime);
          execInfoList.add(execInfo);
        } catch (RejectedExecutionException e) {
          _logger.debug("Task {} was rejected from the thread pool and will be run in the caller thread.", task, e);
          executeDirectly(task, execInfoList);
        } 
      } 
    } 
    return execInfoList;
  }
  
  private <T> void executeDirectly(Callable<T> task, List<ExecutionInfo<T>> execInfoList) {
    T result = null;
    Throwable error = null;
    long startTime = System.currentTimeMillis();
    try {
      result = task.call();
    } catch (Throwable t) {
      error = t;
      _logger.warn("Data Service execution for task: {} has failed", task, t);
    } 
    if (Thread.currentThread().isInterrupted()) {
      long execTimeMillis = System.currentTimeMillis() - startTime;
      _logger.error("Thread received interrupt while blocked for {} ms: {}", 
          Long.valueOf(execTimeMillis), task);
      cancelAll(execInfoList);
      throw new RuntimeException("Data Service execution was cancelled.");
    } 
    assert result == null || error == null;
    applyErrorHandlingPolicy(task, error, execInfoList);
    TaskResultFutureAdapter<T> trfa = new TaskResultFutureAdapter<>(result, error);
    execInfoList.add(new ExecutionInfo<>(trfa, task, startTime));
  }
  
  private <T> void applyErrorHandlingPolicy(Callable<T> task, Throwable error, List<ExecutionInfo<T>> execInfoList) {
    if (error == null || this._errorHandling == ErrorHandlingPolicy.LENIENT)
      return; 
    cancelAll(execInfoList);
    if (error instanceof RuntimeException)
      throw (RuntimeException)error; 
    if (error instanceof Error)
      throw (Error)error; 
    throw new RuntimeException("Execution error while running Data Service task: " + task, error);
  }
  
  private static <T> void cancelAll(List<ExecutionInfo<T>> execInfoList) {
    long endTime = System.currentTimeMillis();
    for (ExecutionInfo<T> execInfo : execInfoList) {
      long execTimeMillis = endTime - execInfo.getStartTime();
      Future<T> future = execInfo.getFuture();
      Callable<T> task = execInfo.getTask();
      if (!future.isDone()) {
        _logger.error("Task was running at interruption moment for {} ms: {}", 
            Long.valueOf(execTimeMillis), task);
        future.cancel(true);
      } 
    } 
  }
  
  private static RuntimeException toAggregatedError(List<Throwable> errors) {
    if (errors.isEmpty())
      return null; 
    if (errors.size() == 1)
      return toRuntimeException(errors.iterator().next()); 
    RuntimeException aggregated = findIllegalArgumentException(errors);
    if (aggregated == null)
      aggregated = new RuntimeException("Data Service execution error."); 
    for (Throwable error : errors) {
      if (aggregated != error)
        aggregated.addSuppressed(error); 
    } 
    return aggregated;
  }
  
  private static Throwable unwrapExecutionException(Exception e) {
    if (e instanceof ExecutionException)
      return e.getCause(); 
    return e;
  }
  
  private static RuntimeException toRuntimeException(Throwable error) {
    if (error instanceof RuntimeException)
      return (RuntimeException)error; 
    return new RuntimeException(error.getMessage(), error);
  }
  
  private static IllegalArgumentException findIllegalArgumentException(List<Throwable> errors) {
    for (Throwable error : errors) {
      if (error instanceof IllegalArgumentException)
        return (IllegalArgumentException)error; 
    } 
    return null;
  }
  
  private static final class TaskResultFutureAdapter<T> implements Future<T> {
    private final T _result;
    
    private final Throwable _error;
    
    public TaskResultFutureAdapter(T result, Throwable error) {
      this._result = result;
      this._error = error;
    }
    
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }
    
    public boolean isCancelled() {
      return false;
    }
    
    public boolean isDone() {
      return true;
    }
    
    public T get() throws ExecutionException {
      if (this._error != null)
        throw new ExecutionException(this._error); 
      return this._result;
    }
    
    public T get(long timeout, TimeUnit unit) throws ExecutionException {
      return get();
    }
  }
  
  private static final class ExecutionInfo<T> {
    private final Future<T> _future;
    
    private final Callable<T> _task;
    
    private final long _startTime;
    
    ExecutionInfo(Future<T> future, Callable<T> task, long startTime) {
      this._future = future;
      this._task = task;
      this._startTime = startTime;
    }
    
    Future<T> getFuture() {
      return this._future;
    }
    
    Callable<T> getTask() {
      return this._task;
    }
    
    long getStartTime() {
      return this._startTime;
    }
  }
}
