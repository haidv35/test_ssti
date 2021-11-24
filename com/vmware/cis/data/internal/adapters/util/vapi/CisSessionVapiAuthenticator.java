package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vapi.protocol.ProtocolConnection;
import java.net.URI;
import java.security.KeyStore;

public final class CisSessionVapiAuthenticator implements VapiAuthenticator {
  private final VapiProtocolConnectionFactory _connectionFactory;
  
  private final URI _endpoint;
  
  private final KeyStore _trustStore;
  
  public CisSessionVapiAuthenticator(VapiProtocolConnectionFactory connectionFactory, URI endpoint, KeyStore trustStore) {
    assert connectionFactory != null;
    assert endpoint != null;
    this._endpoint = endpoint;
    this._trustStore = trustStore;
    this._connectionFactory = connectionFactory;
  }
  
  public VapiSession login(AuthenticationTokenSource credentials) {
    ProtocolConnection connection = this._connectionFactory.connect(this._endpoint, this._trustStore);
    try {
      VapiSessionSource sessionSource = new CisSessionVapiSessionSource(connection.getApiProvider(), credentials);
      RenewableVapiSession session = new RenewableVapiSession(sessionSource);
      return new VapiSessionWithConnection(session, connection);
    } catch (RuntimeException e) {
      connection.disconnect();
      throw e;
    } 
  }
}
