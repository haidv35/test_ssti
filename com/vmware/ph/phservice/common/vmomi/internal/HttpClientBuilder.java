package com.vmware.ph.phservice.common.vmomi.internal;

import com.vmware.ph.phservice.common.Builder;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

public class HttpClientBuilder implements Builder<CloseableHttpClient> {
  private static final int CONNECTION_TIMEOUT_MILLIS = 60000;
  
  private static final int SOCKET_TIMEOUT_MILLIS = 120000;
  
  private static final Log _log = LogFactory.getLog(HttpClientBuilder.class);
  
  private final KeyStore _trustStore;
  
  public HttpClientBuilder(KeyStore trustStore) {
    this._trustStore = trustStore;
  }
  
  public CloseableHttpClient build() {
    TrustStrategy trustStrategy = new KeyStoreBasedTrustStrategy(this._trustStore);
    try {
      SSLContext sslContext = (new SSLContextBuilder()).loadTrustMaterial((TrustStrategy)trustStrategy).build();
      RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(60000).build();
      CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClientBuilder.create().setSSLContext(sslContext).setDefaultRequestConfig(requestConfig).build();
      return httpClient;
    } catch (Exception e) {
      _log.error("Failed to create HTTP client using the VIM trust store.", e);
      return null;
    } 
  }
}
