package com.vmware.ph.phservice.common.ph.http.execute;

import com.vmware.ph.phservice.common.ph.http.retry.Request;
import com.vmware.ph.phservice.common.ph.http.retry.RetryableHttpClient;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpUriRequest;

public class RetryableRequestExecutor implements RequestExecutor {
  public static final long NO_MAX_RETRYABLE_INTERVAL = 0L;
  
  private static final Log _log = LogFactory.getLog(RetryableRequestExecutor.class);
  
  private static final int DEFAULT_REQUEST_NOT_EXECUTED_RESPONSE_CODE = 503;
  
  private final RetryableHttpClient _retryableHttpClient;
  
  private long _maxAcceptableRetryIntervalMillis = 0L;
  
  private boolean _shouldExecuteRequests = true;
  
  public RetryableRequestExecutor(RetryableHttpClient retryableHttpClient) {
    this._retryableHttpClient = retryableHttpClient;
  }
  
  public void setMaxAcceptableRetryInterval(long maxAcceptableRetryIntervalMillis) {
    this._maxAcceptableRetryIntervalMillis = maxAcceptableRetryIntervalMillis;
  }
  
  public int executeRequest(HttpUriRequest request) throws Exception {
    if (!this._shouldExecuteRequests) {
      _log.debug("Request execution will be skipped.");
      return 503;
    } 
    Request<Void> retryableRequest = (new Request.Builder<>()).forRequest(request).forTag("rts-send-request").beforeRetry(retryIntervalMillis -> {
          if (this._maxAcceptableRetryIntervalMillis != 0L && retryIntervalMillis > this._maxAcceptableRetryIntervalMillis)
            throw new RetryTooLargeException("The retry interval " + retryIntervalMillis + " exceeds the maximum interval of " + this._maxAcceptableRetryIntervalMillis + " milliseconds."); 
        }).build();
    try {
      return this._retryableHttpClient.<Void>execute(retryableRequest).getStatusCode();
    } catch (RetryTooLargeException e) {
      _log.warn(e.getMessage());
      this._shouldExecuteRequests = false;
      return 503;
    } 
  }
  
  public void close() throws IOException {
    this._retryableHttpClient.close();
  }
  
  private static class RetryTooLargeException extends RuntimeException {
    RetryTooLargeException(String message) {
      super(message);
    }
  }
}
