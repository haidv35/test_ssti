package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.internal.adapters.util.vapi.RenewableVapiSession;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiAuthenticator;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiProtocolConnectionFactory;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiSession;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiSessionSource;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiSessionWithConnection;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vapi.protocol.ProtocolConnection;
import java.net.URI;
import java.security.KeyStore;

public final class TaggingVapiAuthenticator implements VapiAuthenticator {
  private final URI _endpoint;
  
  private final KeyStore _trustStore;
  
  private final VapiProtocolConnectionFactory _connectionFactory;
  
  public TaggingVapiAuthenticator(VapiProtocolConnectionFactory connectionFactory, URI endpoint, KeyStore trustStore) {
    assert connectionFactory != null;
    assert endpoint != null;
    this._endpoint = endpoint;
    this._trustStore = trustStore;
    this._connectionFactory = connectionFactory;
  }
  
  public VapiSession login(AuthenticationTokenSource credentials) {
    ProtocolConnection connection = this._connectionFactory.connect(this._endpoint, this._trustStore);
    try {
      VapiSessionSource sessionSource = new TaggingVapiSessionSource(connection.getApiProvider(), credentials);
      RenewableVapiSession session = new RenewableVapiSession(sessionSource);
      return new VapiSessionWithConnection(session, connection);
    } catch (RuntimeException e) {
      connection.disconnect();
      throw e;
    } 
  }
}
