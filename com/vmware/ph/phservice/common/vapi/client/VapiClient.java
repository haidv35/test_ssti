package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubConfigurationBase;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.bindings.client.InvocationConfig;
import com.vmware.vapi.bindings.client.RetryPolicy;
import com.vmware.vapi.cis.authn.ProtocolFactory;
import com.vmware.vapi.core.ExecutionContext;
import com.vmware.vapi.protocol.ClientConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import com.vmware.vapi.protocol.ProtocolConnection;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

public class VapiClient implements AutoCloseable {
  private static final int MAX_VAPI_AUTHN_RETRY_ATTEMPTS = 3;
  
  private static final int SOCKET_TIMEOUT_MILLIS = (int)TimeUnit.MINUTES.toMillis(30L);
  
  private final ProtocolConnection _connection;
  
  private final StubFactory _stubFactory;
  
  private final StubConfiguration _stubConfiguration;
  
  private final SecurityContextProvider _securityContextProvider;
  
  private ExecutionContext.SecurityContext _securityContext;
  
  private String _applianceId = null;
  
  public VapiClient(URI apiUri, KeyStore trustStore, SecurityContextProvider securityContextProvider) {
    this._connection = createProtocolConnection(apiUri, trustStore);
    this._securityContextProvider = securityContextProvider;
    this._stubConfiguration = new StubConfiguration();
    this._stubConfiguration.setRetryPolicy(new AuthnRetryPolicyImpl());
    this._stubFactory = new StubFactory(this._connection.getApiProvider());
  }
  
  public <T extends com.vmware.vapi.bindings.Service> T createStub(Class<T> vapiIface) {
    return (T)this._stubFactory.createStub(vapiIface, (StubConfigurationBase)this._stubConfiguration);
  }
  
  public void close() throws Exception {
    try {
      if (this._securityContext != null)
        this._securityContextProvider.deleteSecurityContext(this._stubFactory, this._securityContext); 
    } finally {
      this._connection.disconnect();
    } 
  }
  
  public void setApplianceId(String applianceId) {
    this._applianceId = applianceId;
  }
  
  public String getApplianceId() {
    return this._applianceId;
  }
  
  private static ProtocolConnection createProtocolConnection(URI apiUri, KeyStore trustStore) {
    ClientConfiguration clientConfiguration = (new ClientConfiguration.Builder()).getConfig();
    HttpConfiguration.SslConfiguration sslConfig = (new HttpConfiguration.SslConfiguration.Builder(trustStore)).getConfig();
    HttpConfiguration httpConfiguration = (new HttpConfiguration.Builder()).setSslConfiguration(sslConfig).setSoTimeout(SOCKET_TIMEOUT_MILLIS).getConfig();
    ProtocolFactory protocolFactory = new ProtocolFactory();
    ProtocolConnection protocolConnection = protocolFactory.getHttpConnection(apiUri
        .toString(), clientConfiguration, httpConfiguration);
    return protocolConnection;
  }
  
  private class AuthnRetryPolicyImpl implements RetryPolicy {
    private AuthnRetryPolicyImpl() {}
    
    public RetryPolicy.RetrySpec onInvocationError(RuntimeException error, RetryPolicy.RetryContext retryContext, int invocationAttempt) {
      if (invocationAttempt > 3)
        return null; 
      if (error instanceof com.vmware.vapi.std.errors.Unauthenticated || error instanceof com.vmware.vapi.std.errors.Unauthorized) {
        try {
          VapiClient.this._securityContext = VapiClient.this
            ._securityContextProvider.getSecurityContext(VapiClient.this._stubFactory);
        } catch (Exception e) {
          return null;
        } 
        ExecutionContext newExecutionContext = new ExecutionContext(retryContext.getExecutionContext().retrieveApplicationData(), VapiClient.this._securityContext);
        InvocationConfig invocationConfig = new InvocationConfig(newExecutionContext);
        RetryPolicy.RetrySpec retrySpec = new RetryPolicy.RetrySpec(invocationConfig);
        return retrySpec;
      } 
      return null;
    }
  }
}
