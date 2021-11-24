package com.vmware.ph.phservice.common.server.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestSizeLimitInterceptor implements HandlerInterceptor {
  static final String LARGE_PAYLOAD_ERROR_MESSAGE = "Request content length %d exceeded limit of %d";
  
  private final int _maxContentLengthBytes;
  
  public RequestSizeLimitInterceptor(int maxContentLengthBytes) {
    this._maxContentLengthBytes = maxContentLengthBytes;
  }
  
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    boolean shouldProceedWithNextInterceptor = true;
    int contentLength = request.getContentLength();
    if (contentLength > this._maxContentLengthBytes) {
      shouldProceedWithNextInterceptor = false;
      response.sendError(413, 
          String.format("Request content length %d exceeded limit of %d", new Object[] { Integer.valueOf(contentLength), 
              Integer.valueOf(this._maxContentLengthBytes) }));
    } 
    return shouldProceedWithNextInterceptor;
  }
  
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}
  
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}
