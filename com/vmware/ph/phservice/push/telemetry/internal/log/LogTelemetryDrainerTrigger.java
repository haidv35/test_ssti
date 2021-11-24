package com.vmware.ph.phservice.push.telemetry.internal.log;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

public class LogTelemetryDrainerTrigger implements Trigger {
  private final long _minDelayMillis;
  
  private final long _maxDelayMillis;
  
  public LogTelemetryDrainerTrigger(long minDelayMillis, long maxDelayMillis) {
    this._minDelayMillis = minDelayMillis;
    this._maxDelayMillis = maxDelayMillis;
  }
  
  public Date nextExecutionTime(TriggerContext triggerContext) {
    long nextExecutionDelayMillis = ThreadLocalRandom.current().nextLong(this._minDelayMillis, this._maxDelayMillis);
    long nextExecutionTime = System.currentTimeMillis() + nextExecutionDelayMillis;
    return new Date(nextExecutionTime);
  }
}
