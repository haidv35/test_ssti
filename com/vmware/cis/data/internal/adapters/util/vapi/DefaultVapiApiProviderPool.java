package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.cis.data.internal.adapters.lookup.ServiceEndpointInfo;
import com.vmware.cis.data.internal.adapters.tagging.TaggingVapiAuthenticator;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.MultiSsoDomainAuthenticationTokenSource;
import com.vmware.cis.data.internal.util.SingleSsoDomainAuthenticationTokenSource;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.protocol.ProtocolConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultVapiApiProviderPool implements VapiApiProviderPool {
  public static final class Builder {
    private final Collection<ServiceEndpointInfo> _endpoints;
    
    private VapiProtocolConnectionFactory _connectionFactory = new SharedProtocolConnectionFactory();
    
    private boolean _useTaggingAuthenticator = false;
    
    public static Builder forEndpoints(Collection<ServiceEndpointInfo> endpoints) {
      assert endpoints != null;
      return new Builder(endpoints);
    }
    
    private Builder(Collection<ServiceEndpointInfo> endpoints) {
      assert endpoints != null;
      this._endpoints = endpoints;
    }
    
    public Builder withVapiConnectionFactory(VapiProtocolConnectionFactory connectionFactory) {
      assert connectionFactory != null;
      this._connectionFactory = connectionFactory;
      return this;
    }
    
    public Builder useTaggingAuthenticator() {
      this._useTaggingAuthenticator = true;
      return this;
    }
    
    public VapiApiProviderPool connect(MultiSsoDomainAuthenticationTokenSource credentials) {
      Map<String, SessionAwareVapiConnectionCloseable> connectionsByNodeId = new HashMap<>();
      for (ServiceEndpointInfo endpoint : this._endpoints) {
        AuthenticationTokenSource token = new SingleSsoDomainAuthenticationTokenSource(credentials, endpoint.getSsoDomain());
        addConnection(connectionsByNodeId, endpoint, token);
      } 
      return new DefaultVapiApiProviderPool(connectionsByNodeId);
    }
    
    public VapiApiProviderPool connect(AuthenticationTokenSource token) {
      Map<String, SessionAwareVapiConnectionCloseable> connectionsByNodeId = new HashMap<>();
      for (ServiceEndpointInfo endpoint : this._endpoints)
        addConnection(connectionsByNodeId, endpoint, token); 
      return new DefaultVapiApiProviderPool(connectionsByNodeId);
    }
    
    private void addConnection(Map<String, SessionAwareVapiConnectionCloseable> connectionsByNodeId, ServiceEndpointInfo endpoint, AuthenticationTokenSource token) {
      assert connectionsByNodeId != null;
      assert endpoint != null;
      assert token != null;
      try {
        SessionAwareVapiConnectionCloseable connection = createConnection(endpoint, token);
        connectionsByNodeId.put(endpoint.getNodeId(), connection);
      } catch (Exception e) {
        DefaultVapiApiProviderPool._logger.error("Error while creating vAPI connection to endpoint: {}", endpoint
            .getUrl(), e);
      } 
    }
    
    private SessionAwareVapiConnectionCloseable createConnection(ServiceEndpointInfo endpoint, AuthenticationTokenSource token) {
      assert endpoint != null;
      assert token != null;
      VapiAuthenticator authenticator = this._useTaggingAuthenticator ? new TaggingVapiAuthenticator(this._connectionFactory, endpoint.getUrl(), endpoint.getTrustStore()) : new CisSessionVapiAuthenticator(this._connectionFactory, endpoint.getUrl(), endpoint.getTrustStore());
      VapiSession session = authenticator.login(token);
      ProtocolConnection protocolConnection = this._connectionFactory.connect(endpoint.getUrl(), endpoint.getTrustStore());
      return new SessionAwareVapiConnectionCloseable(protocolConnection, session, endpoint
          .getUrl());
    }
  }
  
  private static final Logger _logger = LoggerFactory.getLogger(DefaultVapiApiProviderPool.class);
  
  private final Map<String, SessionAwareVapiConnectionCloseable> _connectionsByNodeId;
  
  private DefaultVapiApiProviderPool(Map<String, SessionAwareVapiConnectionCloseable> connectionsByNodeId) {
    assert connectionsByNodeId != null;
    this._connectionsByNodeId = connectionsByNodeId;
  }
  
  public ApiProvider getApiProvider(String nodeId) {
    assert nodeId != null;
    SessionAwareVapiConnectionCloseable connection = this._connectionsByNodeId.get(nodeId);
    if (connection == null)
      return null; 
    return connection.getApiProvider();
  }
  
  public void close() {
    for (String nodeId : this._connectionsByNodeId.keySet()) {
      SessionAwareVapiConnectionCloseable connection = this._connectionsByNodeId.get(nodeId);
      try {
        connection.close();
      } catch (Exception e) {
        _logger.error("Error while closing vAPI connection to vCenter node: {}", nodeId, e);
      } 
    } 
  }
}
