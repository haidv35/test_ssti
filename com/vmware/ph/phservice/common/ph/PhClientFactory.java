package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.client.api.PhClient;
import com.vmware.ph.client.api.commondataformat.dimensions.Collector;
import com.vmware.ph.client.api.impl.PhClientBuilder;
import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.ph.config.PhClientConnectionConfiguration;
import com.vmware.ph.phservice.common.ph.config.PhClientRetryStrategyConfiguration;
import java.time.Duration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PhClientFactory {
  private static final Log _logger = LogFactory.getLog(PhClientFactory.class);
  
  private final PhEnvironmentProvider _environmentProvider;
  
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final PhClientConnectionConfiguration _connectionConfiguration;
  
  private final PhClientBuilderFactory _phClientBuilderFactory;
  
  public PhClientFactory(PhEnvironmentProvider environmentProvider, CeipConfigProvider ceipConfigProvider, PhClientConnectionConfiguration connectionConfiguration, PhClientBuilderFactory phClientBuilderFactory) {
    this._environmentProvider = environmentProvider;
    this._ceipConfigProvider = ceipConfigProvider;
    if (connectionConfiguration != null) {
      this._connectionConfiguration = connectionConfiguration;
      _logger.debug("Creating a PhClient with the following connection configuration: " + this._connectionConfiguration);
    } else {
      this._connectionConfiguration = new PhClientConnectionConfiguration();
      _logger.debug("No connection configuration provided. Will create a PhClient with default connection settings: " + this._connectionConfiguration);
    } 
    this._phClientBuilderFactory = phClientBuilderFactory;
  }
  
  public PhClient create(Collector collector) {
    PhClientBuilder builder = this._phClientBuilderFactory.create(this._environmentProvider
        .getEnvironment(), collector);
    builder.setCeipConfigProvider(this._ceipConfigProvider);
    applyConnectionConfigToPhClientBuilder(this._connectionConfiguration, builder);
    return builder.build();
  }
  
  private void applyConnectionConfigToPhClientBuilder(PhClientConnectionConfiguration connectionConfiguration, PhClientBuilder builder) {
    if (connectionConfiguration.getProxySettingsProvider() != null)
      builder.setProxyProvider(connectionConfiguration.getProxySettingsProvider()); 
    HttpConnectionConfig httpConnectionConfig = connectionConfiguration.getHttpConnectionConfig();
    builder.setMaxConnectionsPerRoute(httpConnectionConfig.getMaxConnectionsPerRoute());
    builder.setMaxConnectionsTotal(httpConnectionConfig.getMaxConnectionsTotal());
    builder.setConnectionTimeout(httpConnectionConfig
        .getConnectionTimeout());
    PhClientRetryStrategyConfiguration phClientRetryStrategyConfiguration = connectionConfiguration.getPhClientRetryStrategyConfiguration();
    builder.setUseSeparateManifestExecutor(phClientRetryStrategyConfiguration
        .getUseSeparateRepeatableInvocationStrategy());
    builder.setMinJitterDuration(Duration.ofMillis(phClientRetryStrategyConfiguration.getMinJitterDuration().longValue()));
    builder.setMaxBackoffDuration(Duration.ofMillis(phClientRetryStrategyConfiguration.getMaxBackoffDuration().longValue()));
  }
}
