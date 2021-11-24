package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.protocol.ProtocolConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VapiSessionWithConnection implements VapiSession {
  private static Logger _logger = LoggerFactory.getLogger(VapiSessionWithConnection.class);
  
  private final VapiSession _session;
  
  private final ProtocolConnection _connection;
  
  public VapiSessionWithConnection(VapiSession session, ProtocolConnection connection) {
    assert session != null;
    assert connection != null;
    this._session = session;
    this._connection = connection;
  }
  
  public char[] get() {
    return this._session.get();
  }
  
  public char[] renew(char[] expired) {
    return this._session.renew(expired);
  }
  
  public void logout() {
    try {
      this._session.logout();
    } catch (RuntimeException ex) {
      _logger.debug("Error while logging out vAPI session", ex);
    } 
    try {
      this._connection.disconnect();
    } catch (RuntimeException ex) {
      _logger.debug("Error while closing vAPI connection", ex);
    } 
  }
}
