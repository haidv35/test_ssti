package com.vmware.ph.phservice.common.ph.http.execute;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public class OneTimeRequestExecutor implements RequestExecutor {
  private final CloseableHttpClient _httpClient;
  
  public OneTimeRequestExecutor(CloseableHttpClient httpClient) {
    this._httpClient = httpClient;
  }
  
  public int executeRequest(HttpUriRequest request) throws Exception {
    try (CloseableHttpResponse httpResponse = this._httpClient.execute(request)) {
      return httpResponse.getStatusLine().getStatusCode();
    } 
  }
  
  public void close() throws IOException {
    this._httpClient.close();
  }
}
