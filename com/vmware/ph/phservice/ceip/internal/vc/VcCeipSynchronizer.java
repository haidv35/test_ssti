package com.vmware.ph.phservice.ceip.internal.vc;

import com.vmware.ph.phservice.ceip.internal.CeipApiException;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VcCeipSynchronizer implements CeipSynchronizer {
  private static final Logger _log = LoggerFactory.getLogger(VcCeipSynchronizer.class);
  
  private final int _syncDelayMillis;
  
  private final int _syncPeriodMillis;
  
  private final Timer _timer;
  
  private final SyncConsentManager _syncConsentManager;
  
  private boolean _startMethodBeenExecuted = false;
  
  public VcCeipSynchronizer(int syncPeriodMillis, SyncConsentManager syncConsentManager) {
    this(syncPeriodMillis, syncPeriodMillis, syncConsentManager, new TimerFactory());
  }
  
  VcCeipSynchronizer(int syncDelayMillis, int syncPeriodMillis, SyncConsentManager syncConsentManager, TimerFactory timerFactory) {
    this._syncDelayMillis = syncDelayMillis;
    this._syncPeriodMillis = syncPeriodMillis;
    this._syncConsentManager = syncConsentManager;
    this._timer = timerFactory.createTimer();
  }
  
  public void start() {
    this._startMethodBeenExecuted = true;
    this._timer.scheduleAtFixedRate(new TimerTask() {
          public void run() {
            try {
              int syncDelaySeconds = VcCeipSynchronizer.this._syncDelayMillis / 1000;
              VcCeipSynchronizer.this.sync();
              VcCeipSynchronizer._log.info("Global consent status synchronizer will start again after {} s.", Integer.valueOf(syncDelaySeconds));
            } catch (RuntimeException e) {
              VcCeipSynchronizer._log.error("Global consent status synchronizer has stopped unexpectedly. As a result the local consent state will not be synchronized with other consent states. To recover please verify/fix the exact reason in the cause exception and restart the service", e);
            } 
          }
        }this._syncDelayMillis, this._syncPeriodMillis);
    _log.info("CEIP synchronization scheduleAtFixedRate, delay = {} / period = {}", Integer.valueOf(this._syncDelayMillis), Integer.valueOf(this._syncPeriodMillis));
  }
  
  public void stop() {
    this._timer.cancel();
  }
  
  public boolean hasStartMethodBeenExecuted() {
    return this._startMethodBeenExecuted;
  }
  
  private void sync() {
    if (!this._syncConsentManager.isActive()) {
      _log.debug("The consent state will not be synchronized as this is not a VC node.");
      return;
    } 
    try {
      this._syncConsentManager.sync();
      _log.info("Synchronization with the other PSC nodes finished successfully");
    } catch (CeipApiException e) {
      _log.warn("Error while synchronizing the ConsentConfiguration between different PSC nodes. The detailed reason is contained in the inner exception log. As a result of this problem the current consent configuration is out of sync with the other PSC nodes.", e);
    } 
  }
  
  static class TimerFactory {
    public Timer createTimer() {
      return new Timer("CEIPSynchronizerTimer", true);
    }
  }
}
