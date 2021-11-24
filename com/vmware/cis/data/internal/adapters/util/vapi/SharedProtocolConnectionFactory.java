package com.vmware.cis.data.internal.adapters.util.vapi;

import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.Striped;
import com.vmware.vapi.cis.authn.ProtocolFactory;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.ApiProviderStub;
import com.vmware.vapi.protocol.ClientConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.protocol.ProtocolConnection;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SharedProtocolConnectionFactory implements VapiProtocolConnectionFactory {
  private static final int DEFAULT_MAX_CONNECTIONS = 50;
  
  private static final int DEFAULT_READ_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(60L);
  
  private static final int DEFAULT_KEEP_ALIVE = (int)TimeUnit.SECONDS.toMillis(25L);
  
  private static final Logger logger = LoggerFactory.getLogger(SharedProtocolConnectionFactory.class);
  
  private final ProtocolFactory _protocolFactory = new ProtocolFactory();
  
  private final Striped<Lock> _connectionGuard = Striped.lock(
      Runtime.getRuntime().availableProcessors());
  
  private final ConcurrentMap<Key, PooledConnection> _connectionPool = (new MapMaker())
    .makeMap();
  
  private final int _maxConnections;
  
  private final int _readTimeoutMs;
  
  private final int _keepAlivePeriodMs;
  
  public SharedProtocolConnectionFactory() {
    this(50, DEFAULT_READ_TIMEOUT, DEFAULT_KEEP_ALIVE);
  }
  
  public SharedProtocolConnectionFactory(int maxConnections, int readTimeoutMs, int keepAlivePeriodMs) {
    this._maxConnections = maxConnections;
    this._readTimeoutMs = readTimeoutMs;
    this._keepAlivePeriodMs = keepAlivePeriodMs;
  }
  
  public ProtocolConnection connect(URI endpoint, KeyStore trustStore) {
    Key poolKey = new Key(endpoint, trustStore);
    Lock guard = (Lock)this._connectionGuard.get(poolKey);
    guard.lock();
    try {
      PooledConnection connection = this._connectionPool.get(poolKey);
      if (connection != null) {
        connection.leaseCount++;
        logger.debug("Reusing existing vAPI ProtocolConnection to: {}, trustsore hash: {}. The new lease count is: {}.", new Object[] { endpoint, 
              
              Integer.valueOf(System.identityHashCode(trustStore)), 
              Integer.valueOf(connection.leaseCount) });
      } else {
        logger.info("Creating new vAPI ProtocolConnection to endpoint: {}, trustsore hash: {}", endpoint, 
            Integer.valueOf(System.identityHashCode(trustStore)));
        connection = createNewConnection(endpoint, trustStore);
        this._connectionPool.put(poolKey, connection);
      } 
      return new LeasedConnection(poolKey, connection);
    } finally {
      guard.unlock();
    } 
  }
  
  private PooledConnection createNewConnection(URI endpoint, KeyStore trustStore) {
    ProtocolConnection rawConnection = createRawConnection(endpoint, trustStore);
    return new PooledConnection(rawConnection);
  }
  
  private ProtocolConnection createRawConnection(URI endpoint, KeyStore trustStore) {
    Validate.notNull(endpoint, "The endpoint is required.");
    boolean useSsl = "https".equals(endpoint.getScheme());
    if (useSsl)
      Validate.notNull(trustStore, "trustStore is required for https endpoint"); 
    HttpConfiguration.SslConfiguration sslConfig = useSsl ? new HttpConfiguration.SslConfiguration(trustStore) : null;
    HttpConfiguration httpConfig = (new HttpConfiguration.Builder()).setSslConfiguration(sslConfig).setMaxConnections(this._maxConnections).setSoTimeout(this._readTimeoutMs).setKeepAlivePeriod(this._keepAlivePeriodMs).setLibraryType(HttpConfiguration.LibType.APACHE_HTTP_ASYNC_CLIENT).getConfig();
    ClientConfiguration clientConfiguration = (new ClientConfiguration.Builder()).getConfig();
    return this._protocolFactory.getHttpConnection(endpoint.toString(), clientConfiguration, httpConfig);
  }
  
  private final class LeasedConnection implements ProtocolConnection {
    private final SharedProtocolConnectionFactory.Key _poolKey;
    
    private final SharedProtocolConnectionFactory.PooledConnection _pooledConnection;
    
    private final AtomicBoolean _connected = new AtomicBoolean(true);
    
    public LeasedConnection(SharedProtocolConnectionFactory.Key poolKey, SharedProtocolConnectionFactory.PooledConnection pooledConnection) {
      this._poolKey = poolKey;
      this._pooledConnection = pooledConnection;
    }
    
    public void disconnect() {
      if (!this._connected.getAndSet(false))
        return; 
      boolean mustDisconnect = false;
      Lock guard = (Lock)SharedProtocolConnectionFactory.this._connectionGuard.get(this._poolKey);
      guard.lock();
      try {
        this._pooledConnection.leaseCount--;
        SharedProtocolConnectionFactory.logger.debug("Releasing vAPI ProtocolConnection connection to: {},trustsore hash: {}. The new lease count is: {}.", new Object[] { this._poolKey.endpoint, 

              
              Integer.valueOf(System.identityHashCode(this._poolKey.trustStore)), 
              Integer.valueOf(this._pooledConnection.leaseCount) });
        if (this._pooledConnection.leaseCount <= 0) {
          mustDisconnect = true;
          SharedProtocolConnectionFactory.this._connectionPool.remove(this._poolKey);
        } 
      } finally {
        guard.unlock();
        if (mustDisconnect) {
          SharedProtocolConnectionFactory.logger.info("Closing vAPI ProtocolConnection connection to: {},trustsore hash: {}", this._poolKey.endpoint, 
              
              Integer.valueOf(System.identityHashCode(this._poolKey.trustStore)));
          this._pooledConnection.rawConnection.disconnect();
        } 
      } 
    }
    
    public ApiProvider getApiProvider() {
      return this._pooledConnection.rawConnection.getApiProvider();
    }
    
    public ApiProviderStub getApiProviderStub() {
      return this._pooledConnection.rawConnection.getApiProviderStub();
    }
  }
  
  private static final class PooledConnection {
    public final ProtocolConnection rawConnection;
    
    public int leaseCount;
    
    public PooledConnection(ProtocolConnection connection) {
      this.rawConnection = connection;
      this.leaseCount = 1;
    }
  }
  
  private static final class Key {
    public final URI endpoint;
    
    public final KeyStore trustStore;
    
    public Key(URI endpoint, KeyStore trustStore) {
      this.endpoint = endpoint;
      this.trustStore = trustStore;
    }
    
    public int hashCode() {
      return (new HashCodeBuilder(37, 41))
        .append(this.endpoint)
        .append(System.identityHashCode(this.trustStore))
        .toHashCode();
    }
    
    public boolean equals(Object other) {
      if (other instanceof Key) {
        Key right = (Key)other;
        return (ObjectUtils.equals(this.endpoint, right.endpoint) && 
          ObjectUtils.equals(this.trustStore, right.trustStore));
      } 
      return false;
    }
  }
}
