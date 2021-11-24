package com.vmware.ph.phservice.common.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.HexDump;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogUtil {
  private static final Log _log = LogFactory.getLog(LogUtil.class);
  
  public static String sanitiseForLog(String input) {
    String sanitisedRequest = null;
    if (input == null || input.isEmpty())
      return input; 
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      HexDump.dump(input.getBytes(StandardCharsets.UTF_8), 0L, outputStream, 0);
      sanitisedRequest = outputStream.toString("UTF-8");
    } catch (IOException e) {
      _log.warn("Exception occurred while dumping input for log.", e);
    } 
    return sanitisedRequest;
  }
}
