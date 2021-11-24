package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import java.util.Calendar;
import javax.xml.bind.DatatypeConverter;

public class CalendarUtil {
  public static String formatISO8601(Calendar calendar) {
    return DatatypeConverter.printDateTime(calendar);
  }
}
