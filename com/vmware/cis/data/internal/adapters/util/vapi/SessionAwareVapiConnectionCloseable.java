package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.protocol.ProtocolConnection;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SessionAwareVapiConnectionCloseable implements AutoCloseable {
  private static Logger _logger = LoggerFactory.getLogger(SessionAwareVapiConnectionCloseable.class);
  
  private final ProtocolConnection _protocolConnection;
  
  private final VapiSession _session;
  
  private final ApiProvider _apiProvider;
  
  private final URI _endpoint;
  
  public SessionAwareVapiConnectionCloseable(ProtocolConnection protocolConnection, VapiSession session, URI endpoint) {
    assert protocolConnection != null;
    assert session != null;
    assert endpoint != null;
    this._protocolConnection = protocolConnection;
    this._session = session;
    this._endpoint = endpoint;
    this._apiProvider = new SessionAwareApiProvider(protocolConnection.getApiProvider(), session, endpoint);
  }
  
  public ApiProvider getApiProvider() {
    return this._apiProvider;
  }
  
  public void close() {
    try {
      this._session.logout();
    } catch (RuntimeException ex) {
      _logger.error("Error while logging out vAPI session to endpoint: {}.", this._endpoint, ex);
    } 
    try {
      this._protocolConnection.disconnect();
    } catch (RuntimeException ex) {
      _logger.error("Error while closing vAPI ProtocolConnection to endpoint: {}.", this._endpoint, ex);
    } 
  }
}
