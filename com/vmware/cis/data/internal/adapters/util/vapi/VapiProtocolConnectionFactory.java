package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.protocol.ProtocolConnection;
import java.net.URI;
import java.security.KeyStore;

public interface VapiProtocolConnectionFactory {
  ProtocolConnection connect(URI paramURI, KeyStore paramKeyStore);
}
