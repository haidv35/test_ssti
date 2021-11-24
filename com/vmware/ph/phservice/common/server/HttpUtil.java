package com.vmware.ph.phservice.common.server;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {
  public static boolean isCompressed(HttpServletRequest httpRequest) {
    boolean isCompressed = false;
    String contentEncoding = httpRequest.getHeader("Content-Encoding");
    if ("gzip".equals(contentEncoding))
      isCompressed = true; 
    return isCompressed;
  }
}
