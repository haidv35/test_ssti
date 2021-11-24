package com.vmware.ph.phservice.collector.scheduler;

import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorProvider;
import com.vmware.ph.phservice.collector.internal.scheduler.CollectorLoopExecutionCoordinator;
import com.vmware.ph.phservice.collector.internal.scheduler.ConfigurationException;
import com.vmware.ph.phservice.common.threadstate.ThreadActiveStateManager;
import io.github.resilience4j.timelimiter.TimeLimiter;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultCollectorLoop implements CollectorLoop {
  private static final String DEFAULT_COLLECTOR_THREAD_NAME = "phservices-collector-thread";
  
  private static final String MSG_COLLECTION_START = "Collection process: started";
  
  private static final String MSG_COLLECTION_COMPLETED = "Collection process: completed";
  
  private static final long SETTINGS_CHECK_SECONDS = 60L;
  
  private static final Log _log = LogFactory.getLog(DefaultCollectorLoop.class);
  
  private final AtomicBoolean _mustStop = new AtomicBoolean(false);
  
  private final AtomicBoolean _isRunning = new AtomicBoolean(false);
  
  private final CollectorProvider _collectorProvider;
  
  private final CollectorLoopExecutionConfigProvider _collectorLoopExecutionConfigProvider;
  
  private final CollectorLoopExecutionCoordinator _collectorLoopExecutionCoordinator;
  
  private ScheduledCollectionListener _scheduledCollectionRunListener;
  
  private ThreadActiveStateManager _threadActiveStateManager;
  
  private String _collectorThreadName = "phservices-collector-thread";
  
  private boolean _isCollectionLoopTimeConstrained;
  
  private long _collectorLoopTimeoutMillis;
  
  private Thread _collectorThread;
  
  private Set<CollectionSchedule> _lastCollectionSchedules;
  
  public DefaultCollectorLoop(CollectorProvider collectorProvider, CollectorLoopExecutionConfigProvider collectorLoopExecutionConfigProvider, CollectorLoopExecutionCoordinator collectorLoopExecutionCoordinator) {
    this._collectorProvider = collectorProvider;
    this._collectorLoopExecutionConfigProvider = collectorLoopExecutionConfigProvider;
    this._collectorLoopExecutionCoordinator = collectorLoopExecutionCoordinator;
  }
  
  public void setScheduledCollectionListener(ScheduledCollectionListener scheduledCollectionRunListener) {
    this._scheduledCollectionRunListener = scheduledCollectionRunListener;
  }
  
  public void setThreadActiveStateManager(ThreadActiveStateManager threadActiveStateManager) {
    this._threadActiveStateManager = threadActiveStateManager;
  }
  
  public void setCollectorThreadName(String collectorThreadName) {
    this._collectorThreadName = collectorThreadName;
  }
  
  public synchronized void start() {
    if (this._isRunning.get())
      throw new IllegalStateException(); 
    boolean shouldRunOnce = false;
    if (this._collectorLoopExecutionConfigProvider != null)
      shouldRunOnce = this._collectorLoopExecutionConfigProvider.shouldRunOnce(); 
    final boolean runOnce = shouldRunOnce;
    Runnable collectorThreadTarget = new Runnable() {
        public void run() {
          if (DefaultCollectorLoop.this._threadActiveStateManager != null) {
            long collectorThreadTimeoutSeconds = DefaultCollectorLoop.this._collectorLoopExecutionConfigProvider.getCollectorThreadTimeoutSeconds();
            DefaultCollectorLoop.this._threadActiveStateManager.registerThread(DefaultCollectorLoop.this
                ._collectorThread, collectorThreadTimeoutSeconds);
          } 
          DefaultCollectorLoop.this._isRunning.set(true);
          DefaultCollectorLoop.this.runLoop(runOnce);
          if (DefaultCollectorLoop.this._threadActiveStateManager != null)
            DefaultCollectorLoop.this._threadActiveStateManager.deregisterThread(DefaultCollectorLoop.this._collectorThread); 
        }
      };
    this._collectorThread = new Thread(collectorThreadTarget, this._collectorThreadName);
    this._collectorThread.setDaemon(true);
    this._collectorThread.start();
  }
  
  public synchronized void stop() throws InterruptedException {
    if (this._collectorThread != null) {
      this._collectorThread.interrupt();
      this._collectorThread.join();
      this._collectorThread = null;
    } 
  }
  
  public boolean isRunning() {
    return this._isRunning.get();
  }
  
  protected void runLoop(boolean runOnce) {
    try {
      sleepInitialBackOff();
      while (!this._mustStop.get() && !Thread.currentThread().isInterrupted()) {
        CollectorOutcome outcome;
        try {
          beforeLoopCycle();
          keepAliveCollectorThread();
          Set<CollectionSchedule> schedules = null;
          try {
            if (runOnce) {
              _log.debug("Running collection once...");
            } else {
              _log.debug("Running a collection cycle...");
            } 
            this


              
              ._collectorLoopTimeoutMillis = ((Long)Optional.<CollectorLoopExecutionConfigProvider>ofNullable(this._collectorLoopExecutionConfigProvider).map(provider -> Long.valueOf(Duration.ofSeconds(this._collectorLoopExecutionConfigProvider.getCollectorLoopTimeoutSeconds()).toMillis())).orElse(Long.valueOf(0L))).longValue();
            _log.debug("collectorLoopTimeoutMillis: " + this._collectorLoopTimeoutMillis);
            this._isCollectionLoopTimeConstrained = (this._collectorLoopTimeoutMillis != 0L);
            schedules = this._collectorProvider.getCollectorSchedules();
            outcome = runScheduledCollections(schedules);
            if (runOnce) {
              _log.info(
                  String.format("Stopping the %s, because it was forced to execute just once via property configuration.", new Object[] { getClass().getSimpleName() }));
              this._mustStop.set(true);
            } 
          } catch (Bug e) {
            if (_log.isErrorEnabled())
              _log.error(String.format("BUG: An internal error has occurred: %s. Terminating the collector loop.", new Object[] { e
                      .getMessage() }), (Throwable)e); 
            this._mustStop.set(true);
            return;
          } catch (RuntimeException e) {
            if (_log.isWarnEnabled())
              _log.warn(
                  String.format("Unexpected runtime error escaping the collector loop. The current loop execution will not complete successfully. The loop is not terminated and the next collection attempt will be executed on schedule. For more details on the current error, see the underlying exception: %s: %s", new Object[] { e.getClass().getName(), e
                      .getMessage() }), e); 
            outcome = CollectorOutcome.LOCAL_ERROR;
          } finally {
            if (schedules != null && !runOnce)
              updateCollectionSchedulesStats(schedules); 
          } 
          if (_log.isDebugEnabled())
            _log.debug("The collection cycle completed with outcome: " + outcome); 
        } finally {
          afterLoopCycle();
          keepAliveCollectorThread();
        } 
        sleep(outcome);
      } 
    } catch (InterruptedException e) {
      CollectorOutcome outcome;
      if (_log.isDebugEnabled())
        _log.debug("This thread received the interrupt signal (either due to stop() or container shutting down.) The caller needs us to stop."); 
      this._mustStop.set(true);
    } catch (Exception e) {
      CollectorOutcome outcome;
      if (_log.isErrorEnabled())
        _log.error(
            String.format("Collector terminated unexpectedly due to %s: %s", new Object[] { outcome.getClass().getName(), outcome
                .getMessage() }), (Throwable)outcome); 
    } finally {
      this._isRunning.set(false);
    } 
  }
  
  protected void beforeLoopCycle() {}
  
  protected void afterLoopCycle() {}
  
  CollectorOutcome runScheduledCollections(Set<CollectionSchedule> schedules) throws Exception {
    if (schedules.isEmpty()) {
      _log.warn("There are no scheduled collections to run.");
      return CollectorOutcome.REMOTE_ERROR;
    } 
    Set<CollectorOutcome> collectionOutcomes = new HashSet<>((CollectorOutcome.values()).length);
    for (CollectionSchedule schedule : schedules) {
      CollectorOutcome outcome = runScheduledCollection(schedule);
      collectionOutcomes.add(outcome);
    } 
    for (CollectorOutcome collectorOutcome : collectionOutcomes) {
      if (collectorOutcome != CollectorOutcome.PASSED)
        return collectorOutcome; 
    } 
    return CollectorOutcome.PASSED;
  }
  
  private CollectorOutcome runScheduledCollection(CollectionSchedule schedule) throws Exception {
    if (_log.isDebugEnabled())
      _log.debug("Starting the scheduled collection " + schedule); 
    this._collectorLoopExecutionCoordinator.recordFirstBootTime(System.currentTimeMillis());
    CollectorOutcome outcome = attemptCollection(schedule);
    if (outcome != CollectorOutcome.PASSED)
      if (_log.isInfoEnabled())
        _log.info(String.format("%s collection completed with error.", new Object[] { outcome }));  
    return outcome;
  }
  
  private CollectorOutcome attemptCollection(CollectionSchedule schedule) throws Exception {
    Collector collector = null;
    try {
      boolean isTimeToCollect = this._collectorLoopExecutionCoordinator.isItTimeToCollect(schedule);
      CollectorOutcome outcome = CollectorOutcome.LOCAL_ERROR;
      if (isTimeToCollect) {
        if (_log.isInfoEnabled())
          _log.info("It is time to run the " + schedule + " data collection."); 
        collector = this._collectorProvider.getCollector(schedule);
        if (collector == null) {
          _log.info("Do nothing - Collector is not enabled.");
          return CollectorOutcome.PASSED;
        } 
        long collectionStartTimeMillis = System.currentTimeMillis();
        try {
          if (this._isCollectionLoopTimeConstrained) {
            outcome = performCollectionWithTimeout(collector, schedule, collectionStartTimeMillis);
          } else {
            outcome = performCollection(collector);
          } 
        } finally {
          this._collectorLoopExecutionCoordinator.recordCollectionOutcome(schedule, collectionStartTimeMillis, outcome);
        } 
        notifyScheduledCollectionRunListener(outcome);
      } else {
        if (_log.isInfoEnabled())
          _log.info("Data collector doesn't yet need to run."); 
        outcome = CollectorOutcome.PASSED;
      } 
      return outcome;
    } catch (ConfigurationException e) {
      if (_log.isWarnEnabled())
        _log.warn("Error reading schedule configuration: " + e.getMessage(), e); 
      return CollectorOutcome.LOCAL_ERROR;
    } finally {
      if (collector != null)
        collector.close(); 
    } 
  }
  
  private CollectorOutcome performCollectionWithTimeout(Collector collector, CollectionSchedule schedule, long collectionStartTimeMillis) throws Exception {
    CollectorOutcome outcome;
    if (this._collectorLoopTimeoutMillis == 0L) {
      _log.info("Collection time constraint has been exceeded. Execution of schedule " + schedule + " is skipped.");
      return CollectorOutcome.PASSED;
    } 
    try {
      outcome = (CollectorOutcome)TimeLimiter.of(Duration.ofMillis(this._collectorLoopTimeoutMillis)).executeFutureSupplier(() -> CompletableFuture.supplyAsync(()));
      long collectionEndTimeMillis = System.currentTimeMillis();
      long collectionDurationMillis = collectionEndTimeMillis - collectionStartTimeMillis;
      this._collectorLoopTimeoutMillis -= collectionDurationMillis;
      _log.debug("collectorLoopTimeoutMillis after collection complete: " + this._collectorLoopTimeoutMillis);
    } catch (TimeoutException e) {
      _log.info(String.format("Collection for schedule %s exceeded the time constraint: %s ms. Collection is stopped.", new Object[] { schedule, 

              
              Long.valueOf(this._collectorLoopTimeoutMillis) }));
      outcome = CollectorOutcome.PASSED;
      this._collectorLoopTimeoutMillis = 0L;
    } 
    return outcome;
  }
  
  private static CollectorOutcome performCollection(Collector collector) {
    if (_log.isInfoEnabled())
      _log.info("Collection process: started"); 
    CollectorOutcome collectorOutcome = collector.collect();
    if (_log.isInfoEnabled())
      _log.info("Collection process: completed"); 
    return collectorOutcome;
  }
  
  private void updateCollectionSchedulesStats(Set<CollectionSchedule> schedules) {
    if (!schedules.isEmpty() && (this._lastCollectionSchedules == null || 
      !this._lastCollectionSchedules.equals(schedules))) {
      this._collectorLoopExecutionCoordinator.updateCollectionStats(schedules);
      this._lastCollectionSchedules = schedules;
    } 
  }
  
  private void notifyScheduledCollectionRunListener(CollectorOutcome outcome) {
    if (this._scheduledCollectionRunListener != null) {
      try {
        this._scheduledCollectionRunListener.handle(outcome);
      } catch (Exception e) {
        _log.warn("Failed to call collection run listener!", e);
      } 
    } else {
      _log.debug("No scheduled collection listener to call.");
    } 
  }
  
  private void sleepInitialBackOff() throws InterruptedException {
    sleep(this._collectorLoopExecutionConfigProvider.getInitialBackOffTimeSeconds());
  }
  
  private void sleep(CollectorOutcome outcome) throws InterruptedException {
    long timeSleptSeconds = 0L;
    long totalSeconds = calcSleepInterval(outcome);
    while (timeSleptSeconds < totalSeconds && !this._mustStop.get()) {
      long toSleep = Math.min(totalSeconds - timeSleptSeconds, 60L);
      sleep(toSleep);
      timeSleptSeconds += toSleep;
      totalSeconds = calcSleepInterval(outcome);
    } 
  }
  
  private long calcSleepInterval(CollectorOutcome outcome) {
    switch (outcome) {
      case PASSED:
        seconds = this._collectorLoopExecutionConfigProvider.getScheduleCheckTimeSeconds();
        return seconds;
    } 
    long seconds = this._collectorLoopExecutionConfigProvider.getBackoffTimeSeconds();
    return seconds;
  }
  
  private static void sleep(long sleepIntervalSeconds) throws InterruptedException {
    if (_log.isTraceEnabled())
      _log.trace("Sleeping for " + sleepIntervalSeconds + " seconds ..."); 
    Thread.sleep(sleepIntervalSeconds * 1000L);
  }
  
  private void keepAliveCollectorThread() {
    if (this._threadActiveStateManager != null)
      this._threadActiveStateManager.keepAlive(this._collectorThread); 
  }
}
