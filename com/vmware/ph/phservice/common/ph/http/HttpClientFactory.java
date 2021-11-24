package com.vmware.ph.phservice.common.ph.http;

import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import com.vmware.ph.phservice.common.ph.config.PhClientConnectionConfiguration;
import com.vmware.ph.upload.HttpClientSetup;
import java.util.Optional;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientFactory {
  private final PhClientConnectionConfiguration _connectionConfiguration;
  
  public HttpClientFactory(PhClientConnectionConfiguration connectionConfiguration) {
    this._connectionConfiguration = connectionConfiguration;
  }
  
  public CloseableHttpClient create() {
    HttpConnectionConfig httpConnectionConfig = this._connectionConfiguration.getHttpConnectionConfig();
    ProxySettings proxySettings = Optional.<ProxySettingsProvider>ofNullable(this._connectionConfiguration.getProxySettingsProvider()).map(provider -> provider.getProxySettings(httpConnectionConfig)).orElse(null);
    HttpClientBuilder httpClientBuilder = HttpClientSetup.createHttpClientBuilder(httpConnectionConfig, proxySettings, (CredentialsProvider)new BasicCredentialsProvider());
    return httpClientBuilder.build();
  }
}
