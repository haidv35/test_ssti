package com.vmware.ph.phservice.common.ph.config;

import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettingsProvider;
import java.util.Arrays;
import java.util.Objects;

public class PhClientConnectionConfiguration {
  public static final HttpConnectionConfig DEFAULT_HTTP_CONNECTION_CONFIG = new HttpConnectionConfig(30, 10, 60000, null);
  
  private HttpConnectionConfig _httpConnectionConfig = DEFAULT_HTTP_CONNECTION_CONFIG;
  
  private ProxySettingsProvider _proxySettingsProvider = null;
  
  private PhClientRetryStrategyConfiguration _phClientRetryStrategyConfiguration = new PhClientRetryStrategyConfiguration();
  
  public HttpConnectionConfig getHttpConnectionConfig() {
    return this._httpConnectionConfig;
  }
  
  public void setHttpConnectionConfig(HttpConnectionConfig httpConnectionConfig) {
    this._httpConnectionConfig = httpConnectionConfig;
  }
  
  public ProxySettingsProvider getProxySettingsProvider() {
    return this._proxySettingsProvider;
  }
  
  public void setProxySettingsProvider(ProxySettingsProvider proxySettingsProvider) {
    this._proxySettingsProvider = proxySettingsProvider;
  }
  
  public PhClientRetryStrategyConfiguration getPhClientRetryStrategyConfiguration() {
    return this._phClientRetryStrategyConfiguration;
  }
  
  public void setPhClientRetryStrategyConfiguration(PhClientRetryStrategyConfiguration phClientRetryStrategyConfiguration) {
    this._phClientRetryStrategyConfiguration = phClientRetryStrategyConfiguration;
  }
  
  public String toString() {
    return "PhClientConnectionConfiguration{_httpConnectionConfig=" + this._httpConnectionConfig + ", _proxySettingsProvider=" + this._proxySettingsProvider + ", _phClientRetryStrategyConfiguration=" + this._phClientRetryStrategyConfiguration + '}';
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    PhClientConnectionConfiguration that = (PhClientConnectionConfiguration)o;
    return (httpConnectionConfigEquals(this._httpConnectionConfig, that._httpConnectionConfig) && 
      
      Objects.equals(this._proxySettingsProvider, that._proxySettingsProvider) && this._phClientRetryStrategyConfiguration
      .equals(that._phClientRetryStrategyConfiguration));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this._httpConnectionConfig, this._proxySettingsProvider, this._phClientRetryStrategyConfiguration });
  }
  
  private static boolean httpConnectionConfigEquals(HttpConnectionConfig config, HttpConnectionConfig otherConfig) {
    if (config == null && otherConfig == null)
      return true; 
    if (config != null && otherConfig != null)
      return (config.getConnectionTimeout() == otherConfig.getConnectionTimeout() && config
        .getMaxConnectionsPerRoute() == otherConfig.getMaxConnectionsPerRoute() && config
        .getMaxConnectionsTotal() == otherConfig.getMaxConnectionsTotal() && 
        Arrays.equals((Object[])config.getTrustManagers(), (Object[])otherConfig.getTrustManagers())); 
    return false;
  }
}
