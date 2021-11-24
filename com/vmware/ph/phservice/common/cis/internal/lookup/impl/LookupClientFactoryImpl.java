package com.vmware.ph.phservice.common.cis.internal.lookup.impl;

import com.vmware.ph.phservice.common.cis.internal.lookup.LookupClientFactory;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.Executor;

public class LookupClientFactoryImpl implements LookupClientFactory {
  private static final String LOOKUP_VMODL_PACAKGE_NAME = "com.vmware.vim.binding.lookup";
  
  private final Class<?> _versionClass;
  
  private final Executor _threadPool;
  
  private final VmodlContext _vmodlContext;
  
  private KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private Integer _timeoutMs;
  
  public LookupClientFactoryImpl(Class<?> versionClass, Executor threadPool) {
    this._versionClass = versionClass;
    this._threadPool = threadPool;
    this._vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass("com.vmware.vim.binding.lookup", ServiceRegistration.class, false);
  }
  
  public void setTrustStore(KeyStore trustStore) {
    this._trustStore = trustStore;
  }
  
  public void setThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this._thumbprintVerifier = thumbprintVerifier;
  }
  
  public void setTimeoutMs(int ms) {
    this._timeoutMs = Integer.valueOf(ms);
  }
  
  public LookupClient connectLookup(URI lookupServiceUri) {
    HttpClientConfiguration httpClientConfig = createClientConfiguration();
    LookupClient client = new LookupClientImpl(lookupServiceUri, this._versionClass, httpClientConfig, this._vmodlContext);
    return client;
  }
  
  private HttpClientConfiguration createClientConfiguration() {
    HttpClientConfiguration clientConfig = HttpClientConfiguration.Factory.newInstance();
    HttpConfiguration httpConfiguration = HttpConfiguration.Factory.newInstance();
    httpConfiguration.setTrustStore(this._trustStore);
    httpConfiguration.setThumbprintVerifier(this._thumbprintVerifier);
    httpConfiguration.setCheckStaleConnection(true);
    if (this._timeoutMs != null)
      httpConfiguration.setTimeoutMs(this._timeoutMs.intValue()); 
    clientConfig.setHttpConfiguration(httpConfiguration);
    clientConfig.setExecutor(this._threadPool);
    return clientConfig;
  }
}
