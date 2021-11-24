package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.google.common.collect.MapMaker;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SharedHttpConfigurationFactory implements HttpConfigurationFactory {
  private static final Logger logger = LoggerFactory.getLogger(SharedHttpConfigurationFactory.class);
  
  private static final int DEFAULT_MAX_CONNECTIONS = 500;
  
  private static final int DEFAULT_CONNECT_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(20L);
  
  private static final int DEFAULT_READ_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(60L);
  
  private static final int DEFAULT_KEEP_ALIVE = (int)TimeUnit.SECONDS.toMillis(25L);
  
  private final ConcurrentMap<Key, HttpConfiguration> _existingConfigurations = (new MapMaker())
    .weakValues()
    .makeMap();
  
  private final int _maxConnections;
  
  private final int _connectTimeoutMs;
  
  private final int _readTimeoutMs;
  
  private final int _keepAlivePeriodMs;
  
  public SharedHttpConfigurationFactory() {
    this(500, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_KEEP_ALIVE);
  }
  
  public SharedHttpConfigurationFactory(int maxConnections, int connectTimeoutMs, int readTimeoutMs, int keepAlivePeriodMs) {
    this._maxConnections = maxConnections;
    this._connectTimeoutMs = connectTimeoutMs;
    this._readTimeoutMs = readTimeoutMs;
    this._keepAlivePeriodMs = keepAlivePeriodMs;
  }
  
  public HttpConfiguration createConfiguration(URI serviceAddress, KeyStore trustStore) {
    Validate.notNull(serviceAddress, "The serviceAddress is required.");
    HttpConfiguration httpConfig = HttpConfiguration.Factory.newInstance();
    if (trustStore != null)
      httpConfig.setTrustStore(trustStore); 
    httpConfig.setMaxConnections(this._maxConnections);
    httpConfig.setDefaultMaxConnectionsPerRoute(this._maxConnections);
    httpConfig.setConnectTimeoutMs(this._connectTimeoutMs);
    httpConfig.setTimeoutMs(this._readTimeoutMs);
    httpConfig.setKeepAlivePeriod(this._keepAlivePeriodMs);
    Key key = new Key(serviceAddress, trustStore);
    HttpConfiguration existingConfig = this._existingConfigurations.putIfAbsent(key, httpConfig);
    if (existingConfig == null) {
      logger.debug("Create new HttpConfiguration for endpoint {}", serviceAddress);
      return httpConfig;
    } 
    logger.debug("Reusing existing HttpConfiguration for endpoint {}", serviceAddress);
    return existingConfig;
  }
  
  private static final class Key {
    private final String _host;
    
    private final KeyStore _trustStore;
    
    public Key(URI endpoint, KeyStore trustStore) {
      this._host = endpoint.getHost();
      this._trustStore = trustStore;
    }
    
    public int hashCode() {
      return (new HashCodeBuilder(37, 41))
        .append(this._host)
        .append(System.identityHashCode(this._trustStore))
        .toHashCode();
    }
    
    public boolean equals(Object other) {
      if (other instanceof Key) {
        Key otherKey = (Key)other;
        return (this._host.equals(otherKey._host) && this._trustStore == otherKey._trustStore);
      } 
      return false;
    }
  }
}
