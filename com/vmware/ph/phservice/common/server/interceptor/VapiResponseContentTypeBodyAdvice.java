package com.vmware.ph.phservice.common.server.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

public class VapiResponseContentTypeBodyAdvice implements ResponseBodyAdvice<Object> {
  private static final Log _log = LogFactory.getLog(VapiResponseContentTypeBodyAdvice.class);
  
  public static final String APPLICATION_VAPI_CONTENT_TYPE = "application/vapi";
  
  public static final MediaType APPLICATION_VAPI_MEDIA_TYPE = MediaType.valueOf("application/vapi");
  
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    if (_log.isDebugEnabled())
      _log.debug("Converter type is: " + converterType); 
    return (converterType == MappingJackson2HttpMessageConverter.class);
  }
  
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
    if (response != null) {
      HttpHeaders headers = response.getHeaders();
      if (headers != null) {
        MediaType responseContentType = headers.getContentType();
        if (_log.isDebugEnabled())
          _log.debug("Response content type is: " + responseContentType); 
        if (APPLICATION_VAPI_MEDIA_TYPE.equals(responseContentType)) {
          MediaType contentType = MediaType.APPLICATION_JSON;
          if (_log.isDebugEnabled())
            _log.debug("Overriding response content type with: " + contentType); 
          headers.setContentType(contentType);
        } 
      } 
    } 
    return body;
  }
}
