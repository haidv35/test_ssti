package com.vmware.ph.phservice.common.server.interceptor;

import com.vmware.ph.phservice.common.internal.LogUtil;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestLoggingInterceptor implements HandlerInterceptor {
  private static final Log _log = LogFactory.getLog(RequestLoggingInterceptor.class);
  
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    return true;
  }
  
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("[%s] Received request from [%s] for URI path [%s] with query parameters [%s]", new Object[] { request.getSession().getId(), 
              getClientIp(request), 
              LogUtil.sanitiseForLog(request.getRequestURI()), request
              .getQueryString() })); 
  }
  
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    if (_log.isDebugEnabled())
      try {
        HttpSession session = request.getSession();
        _log.debug(
            String.format("[%s] Responding to [%s] for URI path [%s] with query parameters [%s] with status [%d]", new Object[] { session.getId(), 
                getClientIp(request), 
                LogUtil.sanitiseForLog(request.getRequestURI()), request
                .getQueryString(), 
                Integer.valueOf(response.getStatus()) }));
      } catch (IllegalStateException e) {
        _log.debug(String.format("Response has already been set for [%s] for URI path [%s] with query parameters [%s] with status [%d]", new Object[] { getClientIp(request), 
                LogUtil.sanitiseForLog(request.getRequestURI()), request
                .getQueryString(), 
                Integer.valueOf(response.getStatus()) }));
      }  
  }
  
  private String getClientIp(HttpServletRequest request) {
    return Optional.<String>ofNullable(request.getHeader("X-FORWARDED-FOR"))
      .orElse(request.getRemoteAddr());
  }
}
