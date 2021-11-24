package com.vmware.ph.phservice.collector.internal.scheduler;

import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.collector.scheduler.CollectionScheduleExecutionState;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimeZone;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScheduleCalculator {
  private static final Log _log = LogFactory.getLog(ScheduleCalculator.class);
  
  private static final int MILLIS_IN_MINUTE = 60000;
  
  private static final int MINUTES_IN_HOUR = 60;
  
  private static final int HOURS_IN_DAY = 24;
  
  private static final int DAYS_IN_WEEK = 7;
  
  private final Random rnd = new Random();
  
  private static final SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  static {
    _formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
  
  private static String toUtcTimeString(long timeInMillis) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(timeInMillis);
    return _formatter.format(calendar.getTime());
  }
  
  public boolean isItTimeToRunScheduledCollection(CollectionScheduleExecutionState executionState, long collectionFirstBootMillis, long nowMillis) {
    long lastCollectionTimeMillis = executionState.getLastCollectionCompleteTime();
    lastCollectionTimeMillis = (lastCollectionTimeMillis == 0L) ? collectionFirstBootMillis : lastCollectionTimeMillis;
    if (lastCollectionTimeMillis < 0L || nowMillis < 0L || nowMillis < lastCollectionTimeMillis) {
      _log.error("Error in input parameters for isItTimeToRunScheduledCollection: lastCollectionTime = " + 
          
          toUtcTimeString(lastCollectionTimeMillis) + " ; firstBootTime = " + 
          toUtcTimeString(collectionFirstBootMillis) + " ; nowTime = " + 
          toUtcTimeString(nowMillis));
      return false;
    } 
    CollectionSchedule schedule = executionState.getSchedule();
    String schedulePattern = executionState.getSchedulePattern();
    if (schedulePattern == null) {
      long l = lastCollectionTimeMillis + schedule.getInterval();
      boolean bool = (l <= nowMillis);
      if (_log.isDebugEnabled())
        _log.debug("Collector configuration: timeToCollect = " + bool + " ; schedule =  " + schedule + " ; lastCollectionTime = " + 

            
            toUtcTimeString(lastCollectionTimeMillis) + " ; nowTime = " + 
            toUtcTimeString(nowMillis) + " ; nextFire = " + 
            toUtcTimeString(l)); 
      return bool;
    } 
    Calendar nextCollectionTime = getNextCollectionTime(lastCollectionTimeMillis, schedulePattern, schedule);
    assert lastCollectionTimeMillis < nextCollectionTime.getTimeInMillis();
    Calendar currentTime = buildCalendarInTheRightTimezone(nowMillis);
    boolean isItTimeToCollect = (nextCollectionTime.before(currentTime) || nextCollectionTime.equals(currentTime));
    if (_log.isDebugEnabled())
      _log.debug("Collector configuration: timeToCollect = " + isItTimeToCollect + " ; schedule =  " + schedule + " ; schedulePattern =  " + schedulePattern + " ; lastCollectionTime = " + 


          
          toUtcTimeString(lastCollectionTimeMillis) + " ; nowTime = " + 
          toUtcTimeString(nowMillis) + " ; nextFire = " + 
          toUtcTimeString(nextCollectionTime.getTimeInMillis())); 
    return isItTimeToCollect;
  }
  
  public boolean isItTimeToRetryCollection(CollectionScheduleExecutionState executionState, long nowMillis) {
    CollectionSchedule schedule = executionState.getSchedule();
    int collectionFailureCount = executionState.getFailureCount();
    if (!schedule.getShouldRetryOnFailure() || collectionFailureCount == 0)
      return false; 
    boolean isItTimeToRetryCollection = false;
    if (collectionFailureCount <= schedule.getMaxRetriesCount()) {
      long calculatedRetryIntervalMillis = schedule.getRetryInterval() * collectionFailureCount;
      long lastCollectionMillis = executionState.getLastCollectionCompleteTime();
      long expectedCollectionRetryTimeMillis = lastCollectionMillis + calculatedRetryIntervalMillis;
      isItTimeToRetryCollection = (expectedCollectionRetryTimeMillis < nowMillis);
    } 
    if (_log.isDebugEnabled())
      _log.debug("Collector configuration: timeToRetryCollection = " + isItTimeToRetryCollection + " ; schedule =  " + schedule + " ; collectionFailureCount =  " + executionState


          
          .getFailureCount() + " ; lastCollectionTime = " + 
          
          toUtcTimeString(executionState.getLastCollectionCompleteTime()) + " ; nowTime = " + 
          toUtcTimeString(nowMillis)); 
    return isItTimeToRetryCollection;
  }
  
  public Calendar validateScheduleAndBuildCalendar(String schedulePattern, boolean onErrorThrow) {
    boolean isValid;
    String[] values = StringUtils.split(schedulePattern, " ");
    int numArgs = ArrayUtils.getLength(values);
    if (numArgs < 3)
      return onErrorThrow ? invalidSchedule(schedulePattern, null) : null; 
    Calendar calendar = null;
    Throwable errorCause = null;
    try {
      String sMinute = values[numArgs - 1];
      String sHour = values[numArgs - 2];
      String sDayOfWeek = values[numArgs - 3];
      int minute = Integer.parseInt(sMinute);
      int hour = Integer.parseInt(sHour);
      int dayOfWeek = Integer.parseInt(sDayOfWeek);
      isValid = (0 <= minute && minute <= 59 && 0 <= hour && hour <= 23 && 0 <= dayOfWeek && dayOfWeek <= 6);
      if (isValid) {
        calendar = buildCalendarInTheRightTimezone(0L);
        calendar.set(7, dayOfWeek + 1);
        calendar.set(11, hour);
        calendar.set(12, minute);
        calendar.set(14, 0);
        calendar.clear(14);
      } 
    } catch (NumberFormatException e) {
      calendar = null;
      isValid = false;
      errorCause = e;
    } 
    return (onErrorThrow && !isValid) ? invalidSchedule(schedulePattern, errorCause) : calendar;
  }
  
  public String generateRandomSchedulePattern() {
    int minute = this.rnd.nextInt(60);
    int hour = this.rnd.nextInt(24);
    int dayOfWeek = this.rnd.nextInt(7);
    return dayOfWeek + " " + hour + " " + minute;
  }
  
  private Calendar getNextCollectionTime(long lastCollectionMillis, String schedulePattern, CollectionSchedule schedule) {
    long expectedCollectionTimeMillis = lastCollectionMillis - lastCollectionMillis % 60000L;
    long scheduleIntervalMillis = schedule.getInterval();
    expectedCollectionTimeMillis += scheduleIntervalMillis;
    Calendar nextCollectionTime = buildCalendarInTheRightTimezone(expectedCollectionTimeMillis);
    Calendar scheduleTime = validateScheduleAndBuildCalendar(schedulePattern, true);
    adjustNextCollectionFieldIfScheduleMeasuredInTargetInterval(nextCollectionTime, scheduleTime, schedule, 3600000L, 12, 59);
    adjustNextCollectionFieldIfScheduleMeasuredInTargetInterval(nextCollectionTime, scheduleTime, schedule, 86400000L, 11, 23);
    adjustNextCollectionFieldIfScheduleMeasuredInTargetInterval(nextCollectionTime, scheduleTime, schedule, 604800000L, 7, 6);
    return nextCollectionTime;
  }
  
  private static void adjustNextCollectionFieldIfScheduleMeasuredInTargetInterval(Calendar toBeAdjusted, Calendar target, CollectionSchedule schedule, long targetIntervalMillis, int calendarField, int maxAdjustmentSteps) {
    boolean canMeasureIntervalInTargetInterval = canMeasureScheduleInTargetInterval(schedule, targetIntervalMillis);
    if (canMeasureIntervalInTargetInterval)
      decreaseUntilCalendarFieldMatches(toBeAdjusted, target, calendarField, maxAdjustmentSteps); 
  }
  
  private static void decreaseUntilCalendarFieldMatches(Calendar toBeDecreased, Calendar target, int calendarField, int maxDecreaseSteps) {
    for (int i = 0; i <= maxDecreaseSteps; i++) {
      int toBeDecreasedValue = toBeDecreased.get(calendarField);
      int targetValue = target.get(calendarField);
      if (toBeDecreasedValue == targetValue)
        break; 
      toBeDecreased.add(calendarField, -1);
    } 
  }
  
  private static boolean canMeasureScheduleInTargetInterval(CollectionSchedule schedule, long targetIntervalMillis) {
    return (schedule.getInterval() % targetIntervalMillis == 0L);
  }
  
  private static Calendar buildCalendarInTheRightTimezone(long calendarTimeInMillis) {
    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    c.setTimeInMillis(calendarTimeInMillis);
    return c;
  }
  
  private static Calendar invalidSchedule(String schedule, Throwable cause) {
    throw new ConfigurationException("Provided schedule is invalid: " + schedule, cause);
  }
}
