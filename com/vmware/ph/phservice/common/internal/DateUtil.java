package com.vmware.ph.phservice.common.internal;

import java.util.Calendar;
import java.util.TimeZone;

public final class DateUtil {
  private static final String UTC_TIMEZONE_STRING = "UTC";
  
  public static Calendar createUtcCalendar() {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  }
}
