package com.vmware.ph.phservice.common.internal;

import com.vmware.ph.phservice.common.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class TimeIntervalUtil {
  public static List<Pair<Calendar, Calendar>> splitInterval(@Nonnull Pair<Calendar, Calendar> interval, long stepMillis) {
    if (stepMillis <= 0L)
      return Collections.emptyList(); 
    long startTimeMillis = ((Calendar)interval.getFirst()).getTimeInMillis();
    long endTimeMillis = ((Calendar)interval.getSecond()).getTimeInMillis();
    if (startTimeMillis > endTimeMillis)
      return Collections.emptyList(); 
    if (endTimeMillis - startTimeMillis < stepMillis)
      return Arrays.asList((Pair<Calendar, Calendar>[])new Pair[] { createIntervalFromTimestamps(startTimeMillis, endTimeMillis) }); 
    List<Pair<Calendar, Calendar>> stepIntervals = new ArrayList<>();
    long currentTimeMillis = startTimeMillis;
    while (currentTimeMillis < endTimeMillis) {
      if (currentTimeMillis + stepMillis >= endTimeMillis) {
        stepIntervals.add(
            createIntervalFromTimestamps(currentTimeMillis, endTimeMillis));
      } else {
        stepIntervals.add(
            createIntervalFromTimestamps(currentTimeMillis, currentTimeMillis + stepMillis));
      } 
      currentTimeMillis += stepMillis;
    } 
    return stepIntervals;
  }
  
  public static Pair<Calendar, Calendar> createIntervalFromTimestamps(long startTimeMillis, long endTimeMillis) {
    return new Pair<>(
        convertTimeInMsToCalendar(startTimeMillis), 
        convertTimeInMsToCalendar(endTimeMillis));
  }
  
  public static Calendar convertTimeInMsToCalendar(String timeInMillisStr) {
    long timeInMillis = Long.parseLong(timeInMillisStr);
    return convertTimeInMsToCalendar(timeInMillis);
  }
  
  public static Calendar convertOffsetTimeInMsToCalendar(String offsetTimeInMillisStr) {
    long offsetTimeInMillis = Long.parseLong(offsetTimeInMillisStr);
    Calendar calendar = DateUtil.createUtcCalendar();
    calendar.setTimeInMillis(calendar.getTimeInMillis() + offsetTimeInMillis);
    return calendar;
  }
  
  private static Calendar convertTimeInMsToCalendar(long timeInMillis) {
    Calendar calendar = DateUtil.createUtcCalendar();
    calendar.setTimeInMillis(timeInMillis);
    return calendar;
  }
}
