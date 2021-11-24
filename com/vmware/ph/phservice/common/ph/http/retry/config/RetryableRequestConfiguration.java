package com.vmware.ph.phservice.common.ph.http.retry.config;

import java.util.Objects;

public class RetryableRequestConfiguration {
  private final int _retries;
  
  private final ExponentialBackoffConfiguration _exponentialBackoffConfiguration;
  
  public RetryableRequestConfiguration(int retries, ExponentialBackoffConfiguration exponentialBackoffConfiguration) {
    if (retries < 1)
      throw new IllegalArgumentException("The number of retries must be greater than 1."); 
    this._retries = retries;
    this
      ._exponentialBackoffConfiguration = Objects.<ExponentialBackoffConfiguration>requireNonNull(exponentialBackoffConfiguration, "An exponential backoff configuration must be provided.");
  }
  
  public int getRetries() {
    return this._retries;
  }
  
  public ExponentialBackoffConfiguration getExponentialBackoffConfiguration() {
    return this._exponentialBackoffConfiguration;
  }
}
