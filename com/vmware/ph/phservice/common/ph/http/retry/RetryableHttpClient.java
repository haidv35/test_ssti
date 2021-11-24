package com.vmware.ph.phservice.common.ph.http.retry;

import com.google.common.collect.Sets;
import com.vmware.ph.phservice.common.ph.http.retry.config.ExponentialBackoffConfiguration;
import com.vmware.ph.phservice.common.ph.http.retry.config.RetryableRequestConfiguration;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableHttpClient implements AutoCloseable {
  private static final Logger _logger = LoggerFactory.getLogger(RetryableHttpClient.class);
  
  private static final Set<Integer> BACK_PRESSURE_STATUS_CODES = Collections.unmodifiableSet(Sets.newHashSet((Object[])new Integer[] { Integer.valueOf(429), Integer.valueOf(503) }));
  
  private static final Set<Integer> RETRY_STATUS_CODES = Collections.unmodifiableSet(
      (Set<? extends Integer>)Sets.union(BACK_PRESSURE_STATUS_CODES, Sets.newHashSet((Object[])new Integer[] { Integer.valueOf(504), Integer.valueOf(500) })));
  
  private final CloseableHttpClient _client;
  
  private final RetryableRequestConfiguration _retryableRequestConfiguration;
  
  public RetryableHttpClient(CloseableHttpClient client, RetryableRequestConfiguration retryableRequestConfiguration) {
    this._client = Objects.<CloseableHttpClient>requireNonNull(client, "An HTTP client must be provided.");
    this
      ._retryableRequestConfiguration = Objects.<RetryableRequestConfiguration>requireNonNull(retryableRequestConfiguration, "The request retry configuration must be provided.");
  }
  
  public <T> Response<T> execute(Request<T> request) throws Exception {
    RetryableRequestIntervalFunction intervalFunction = new RetryableRequestIntervalFunction(this._retryableRequestConfiguration);
    Retry retry = buildRetryDecorator(request.getRequestTag(), intervalFunction);
    return Retry.decorateCallable(retry, () -> makeRequest(request, intervalFunction)).call();
  }
  
  public void close() throws IOException {
    this._client.close();
  }
  
  private <T> Retry buildRetryDecorator(String requestTag, IntervalFunction intervalFunction) {
    RetryConfig retryConfig = RetryConfig.custom().maxAttempts(this._retryableRequestConfiguration.getRetries() + 1).retryOnResult(this::shouldRetryOnResponse).intervalFunction(intervalFunction).ignoreExceptions(new Class[] { RuntimeException.class, Exception.class }).build();
    return Retry.of(requestTag, retryConfig);
  }
  
  private <T> Response<T> makeRequest(Request<T> request, RetryableRequestIntervalFunction intervalFunction) throws IOException {
    _logger.info("Executing retryable request.");
    return (Response<T>)this._client.execute(request.getRequest(), response -> {
          int statusCode = response.getStatusLine().getStatusCode();
          T result = null;
          if (statusCode >= 200 && statusCode < 300) {
            _logger.info("Invoking success response handler.");
            result = (T)request.getSuccessResponseHandler().handleResponse(response);
          } else if (RETRY_STATUS_CODES.contains(Integer.valueOf(statusCode))) {
            long retryAfterMillis = getRetryAfterMillisFromResponse(response).longValue();
            intervalFunction.updateInterval(retryAfterMillis);
            _logger.info("Invoking before retry response handler.");
            request.getBeforeRetryHandler().handle(intervalFunction.getInterval());
          } else {
            _logger.info("Invoking error response handler.");
            result = (T)request.getErrorResponseHandler().handleResponse(response);
          } 
          return new Response<>(statusCode, result);
        });
  }
  
  private <T> boolean shouldRetryOnResponse(Response<T> response) {
    int responseCode = response.getStatusCode();
    boolean shouldRetry = RETRY_STATUS_CODES.contains(Integer.valueOf(responseCode));
    _logger.info("Received status code {}. Should operation be retried:  {}", Integer.valueOf(responseCode), Boolean.valueOf(shouldRetry));
    return shouldRetry;
  }
  
  private Long getRetryAfterMillisFromResponse(HttpResponse response) {
    return Optional.<Header>ofNullable(response.getFirstHeader("Retry-After"))
      .map(header -> Long.valueOf(Long.parseLong(header.getValue())))
      .orElse(Long.valueOf(0L));
  }
  
  private static class RetryableRequestIntervalFunction implements IntervalFunction {
    static final long NO_RETRY_INTERVAL_MILLIS = 0L;
    
    private static final int STARTING_BACK_OFF_ATTEMPTS = 1;
    
    private final IntervalFunction _exponentialBackoffIntervalFunction;
    
    private long _retryIntervalMillis = 0L;
    
    private int _backoffAttempts = 1;
    
    RetryableRequestIntervalFunction(RetryableRequestConfiguration retryableRequestConfiguration) {
      ExponentialBackoffConfiguration backoffConfiguration = retryableRequestConfiguration.getExponentialBackoffConfiguration();
      this._exponentialBackoffIntervalFunction = IntervalFunction.ofExponentialRandomBackoff(backoffConfiguration
          .getInitialIntervalMillis(), backoffConfiguration
          .getMultiplier(), backoffConfiguration
          .getRandomizationFactor());
    }
    
    public Long apply(Integer attempts) {
      return Long.valueOf(getInterval());
    }
    
    void updateInterval(long retryIntervalMillis) {
      if (retryIntervalMillis != 0L) {
        this._retryIntervalMillis = retryIntervalMillis;
        this._backoffAttempts = 1;
      } else {
        this._retryIntervalMillis = ((Long)this._exponentialBackoffIntervalFunction.apply(Integer.valueOf(this._backoffAttempts))).longValue();
        this._backoffAttempts++;
      } 
      RetryableHttpClient._logger.info("Retry interval is:  {}", Long.valueOf(this._retryIntervalMillis));
    }
    
    long getInterval() {
      return this._retryIntervalMillis;
    }
  }
}
