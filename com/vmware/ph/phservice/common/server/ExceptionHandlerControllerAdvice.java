package com.vmware.ph.phservice.common.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice extends ResponseEntityExceptionHandler {
  private static final Log _log = LogFactory.getLog(ExceptionHandlerControllerAdvice.class);
  
  private static final String GENERIC_ERROR_MESSAGE = "The application threw an unexpected exception";
  
  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleException(Exception e) {
    _log.error("The application threw an unexpected exception", e);
    return handleException(HttpStatus.INTERNAL_SERVER_ERROR, "The application threw an unexpected exception");
  }
  
  private static ResponseEntity<Object> handleException(HttpStatus httpStatus, String errorMessage) {
    String errorResponseBody = String.format("\"%s\"", new Object[] { errorMessage });
    return new ResponseEntity(errorResponseBody, httpStatus);
  }
}
