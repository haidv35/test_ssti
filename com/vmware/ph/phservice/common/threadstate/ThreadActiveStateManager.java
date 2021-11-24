package com.vmware.ph.phservice.common.threadstate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadActiveStateManager {
  private static final Log _log = LogFactory.getLog(ThreadActiveStateManager.class);
  
  private static final int MIN_TIMEOUT_SECONDS = 1;
  
  private static final String ERROR_INVALID_TIMEOUT = String.format("Thread timeout should be at least %d second(s).", new Object[] { Integer.valueOf(1) });
  
  private Map<Long, ThreadState> _threadIdToThreadState = new ConcurrentHashMap<>();
  
  public void registerThread(Thread thread, long timeoutSeconds) {
    if (timeoutSeconds < 1L)
      throw new IllegalArgumentException(ERROR_INVALID_TIMEOUT); 
    ThreadState threadState = new ThreadState(thread, timeoutSeconds, System.currentTimeMillis());
    this._threadIdToThreadState.put(Long.valueOf(thread.getId()), threadState);
  }
  
  public void deregisterThread(Thread thread) {
    this._threadIdToThreadState.remove(Long.valueOf(thread.getId()));
  }
  
  public void keepAlive(Thread thread) {
    ThreadState threadState = this._threadIdToThreadState.get(Long.valueOf(thread.getId()));
    threadState.setLastHeartbeatTimeMillis(System.currentTimeMillis());
  }
  
  public boolean hasInactiveThreads() {
    boolean hasInactiveThreads = false;
    for (ThreadState threadState : this._threadIdToThreadState.values()) {
      if (!isThreadActive(threadState)) {
        hasInactiveThreads = true;
        break;
      } 
    } 
    return hasInactiveThreads;
  }
  
  ThreadState getThreadStateByThreadId(long id) {
    return this._threadIdToThreadState.get(Long.valueOf(id));
  }
  
  private boolean isThreadActive(ThreadState threadState) {
    boolean isActive = false;
    Thread wrappedThread = threadState.getWrappedThread();
    if (wrappedThread.isAlive() && !isThreadTimedOut(threadState))
      isActive = true; 
    if (!isActive)
      _log.warn(
          String.format("#%d | %s | state [%s] | the thread is hung (not refreshed since %d seconds) or not alive", new Object[] { Long.valueOf(wrappedThread.getId()), wrappedThread
              .getName(), wrappedThread
              .getState().toString(), 
              Long.valueOf(threadState.getTimeoutSeconds()) })); 
    return isActive;
  }
  
  private boolean isThreadTimedOut(ThreadState threadState) {
    long timeSinceLastHeartbeatMillis = System.currentTimeMillis() - threadState.getLastHeartbeatTimeMillis();
    long timeoutInMillis = TimeUnit.SECONDS.toMillis(threadState.getTimeoutSeconds());
    return (timeSinceLastHeartbeatMillis > timeoutInMillis);
  }
  
  static class ThreadState {
    private final Thread _wrappedThread;
    
    private final AtomicLong _lastHeartbeatTimeMillis;
    
    private final long _timeoutSeconds;
    
    public ThreadState(Thread wrappedThread, long timeoutSeconds, long lastHeartbeatTimeMillis) {
      this._wrappedThread = wrappedThread;
      this._timeoutSeconds = timeoutSeconds;
      this._lastHeartbeatTimeMillis = new AtomicLong(lastHeartbeatTimeMillis);
    }
    
    public long getLastHeartbeatTimeMillis() {
      return this._lastHeartbeatTimeMillis.get();
    }
    
    public void setLastHeartbeatTimeMillis(long lastHeartbeatTimeMillis) {
      this._lastHeartbeatTimeMillis.set(lastHeartbeatTimeMillis);
    }
    
    public long getTimeoutSeconds() {
      return this._timeoutSeconds;
    }
    
    public Thread getWrappedThread() {
      return this._wrappedThread;
    }
  }
}
