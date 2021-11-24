package com.vmware.ph.phservice.common.ph.http.execute;

import com.vmware.ph.phservice.common.ph.http.HttpClientFactory;
import com.vmware.ph.phservice.common.ph.http.retry.RetryableHttpClient;
import com.vmware.ph.phservice.common.ph.http.retry.config.RetryableRequestConfiguration;
import java.util.Objects;
import org.apache.http.impl.client.CloseableHttpClient;

public class RequestExecutorFactory {
  private final HttpClientFactory _httpClientFactory;
  
  private RetryableRequestConfiguration _retryableRequestConfiguration;
  
  private long _maxAcceptableRetryIntervalMillis = 0L;
  
  public RequestExecutorFactory(HttpClientFactory httpClientFactory) {
    this._httpClientFactory = Objects.<HttpClientFactory>requireNonNull(httpClientFactory);
  }
  
  public void setRetryableRequestConfiguration(RetryableRequestConfiguration retryableRequestConfiguration) {
    this._retryableRequestConfiguration = retryableRequestConfiguration;
  }
  
  public void setMaxAcceptableRetryIntervalMillis(long maxAcceptableRetryIntervalMillis) {
    this._maxAcceptableRetryIntervalMillis = maxAcceptableRetryIntervalMillis;
  }
  
  public RequestExecutor create() {
    RequestExecutor requestExecutor;
    CloseableHttpClient httpClient = this._httpClientFactory.create();
    if (this._retryableRequestConfiguration != null) {
      requestExecutor = new RetryableRequestExecutor(new RetryableHttpClient(httpClient, this._retryableRequestConfiguration));
      ((RetryableRequestExecutor)requestExecutor)
        .setMaxAcceptableRetryInterval(this._maxAcceptableRetryIntervalMillis);
    } else {
      requestExecutor = new OneTimeRequestExecutor(httpClient);
    } 
    return requestExecutor;
  }
}
