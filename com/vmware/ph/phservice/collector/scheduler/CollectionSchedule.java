package com.vmware.ph.phservice.collector.scheduler;

import java.util.concurrent.TimeUnit;

public final class CollectionSchedule implements Comparable<CollectionSchedule> {
  private static final int MAX_RETRIES_COUNT = 3;
  
  public static final CollectionSchedule WEEKLY = new CollectionSchedule(TimeUnit.DAYS
      
      .toMillis(7L), TimeUnit.HOURS
      
      .toMillis(2L), 3);
  
  public static final CollectionSchedule DAILY = new CollectionSchedule(TimeUnit.DAYS
      
      .toMillis(1L), TimeUnit.HOURS
      
      .toMillis(2L), 3);
  
  private static final int MINUTE_TO_MILLIS = 60000;
  
  public static final int HOUR_TO_MILLIS = 3600000;
  
  public static final int DAY_TO_MILLIS = 86400000;
  
  public static final int WEEK_TO_MILLIS = 604800000;
  
  private static final String INTERVAL_AS_STRING_PATTERN = "%d%s";
  
  private final long _intervalMillis;
  
  private final long _retryIntervalMillis;
  
  private final int _maxRetriesCount;
  
  public CollectionSchedule(long intervalMillis) {
    this(intervalMillis, 0L, 0);
  }
  
  public CollectionSchedule(long intervalMillis, long retryIntervalMillis, int maxRetriesCount) {
    this._intervalMillis = intervalMillis;
    this._retryIntervalMillis = retryIntervalMillis;
    this._maxRetriesCount = maxRetriesCount;
  }
  
  public final long getInterval() {
    return this._intervalMillis;
  }
  
  public String getIntervalAsString() {
    return getMillisAsTimeString(this._intervalMillis);
  }
  
  public long getRetryInterval() {
    return this._retryIntervalMillis;
  }
  
  public String getRetryIntervalAsString() {
    return getMillisAsTimeString(this._retryIntervalMillis);
  }
  
  public int getMaxRetriesCount() {
    return this._maxRetriesCount;
  }
  
  public boolean getShouldRetryOnFailure() {
    return (this._maxRetriesCount > 0 && this._retryIntervalMillis > 0L);
  }
  
  public int compareTo(CollectionSchedule o) {
    int compareInterval = Long.compare(getInterval(), o.getInterval());
    if (compareInterval != 0)
      return compareInterval; 
    int compareRetryInterval = Long.compare(getRetryInterval(), o.getRetryInterval());
    if (compareRetryInterval != 0)
      return compareRetryInterval; 
    return Integer.compare(getMaxRetriesCount(), o.getMaxRetriesCount());
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + (int)(this._intervalMillis ^ this._intervalMillis >>> 32L);
    result = 31 * result + this._maxRetriesCount;
    result = 31 * result + (int)(this._retryIntervalMillis ^ this._retryIntervalMillis >>> 32L);
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    CollectionSchedule other = (CollectionSchedule)obj;
    if (this._intervalMillis != other._intervalMillis)
      return false; 
    if (this._maxRetriesCount != other._maxRetriesCount)
      return false; 
    if (this._retryIntervalMillis != other._retryIntervalMillis)
      return false; 
    return true;
  }
  
  public String toString() {
    return String.format("CollectionSchedule [interval=%s, retryInterval=%s, maxRetriesCount=%d]", new Object[] { getIntervalAsString(), 
          getRetryIntervalAsString(), 
          Integer.valueOf(this._maxRetriesCount) });
  }
  
  private static String getMillisAsTimeString(long milliseconds) {
    int weeks = (int)(milliseconds / 604800000L);
    if (weeks > 0)
      return String.format("%d%s", new Object[] { Integer.valueOf(weeks), "w" }); 
    int days = (int)(milliseconds / 86400000L % 7L);
    if (days > 0)
      return String.format("%d%s", new Object[] { Integer.valueOf(days), "d" }); 
    int hours = (int)(milliseconds / 3600000L % 24L);
    if (hours > 0)
      return String.format("%d%s", new Object[] { Integer.valueOf(hours), "h" }); 
    int minutes = (int)(milliseconds / 60000L % 60L);
    return String.format("%d%s", new Object[] { Integer.valueOf(minutes), "m" });
  }
}
