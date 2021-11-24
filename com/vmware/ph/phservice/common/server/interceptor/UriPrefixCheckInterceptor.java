package com.vmware.ph.phservice.common.server.interceptor;

import com.vmware.ph.phservice.common.internal.LogUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class UriPrefixCheckInterceptor implements HandlerInterceptor {
  private static final Log _log = LogFactory.getLog(UriPrefixCheckInterceptor.class);
  
  private final String _allowedRequestPathPrefix;
  
  public UriPrefixCheckInterceptor(String allowedRequestPathPrefix) {
    this._allowedRequestPathPrefix = allowedRequestPathPrefix.replace("*", "");
  }
  
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    boolean shouldProceedWithNextInterceptor = true;
    if (this._allowedRequestPathPrefix != null && 
      !request.getRequestURI().startsWith(this._allowedRequestPathPrefix)) {
      _log.debug(String.format("Rejecting the following request due to an incorrect URI path [%s]. Expected path: [%s] ", new Object[] { LogUtil.sanitiseForLog(request.getRequestURI()), this._allowedRequestPathPrefix }));
      response.sendError(404);
      shouldProceedWithNextInterceptor = false;
    } 
    return shouldProceedWithNextInterceptor;
  }
}
