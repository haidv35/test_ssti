package com.vmware.ph.phservice.collector.internal.scheduler;

public class ConfigurationException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ConfigurationException(String message) {
    super(message);
  }
}
