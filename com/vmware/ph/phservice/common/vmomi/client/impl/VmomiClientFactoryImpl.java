package com.vmware.ph.phservice.common.vmomi.client.impl;

import com.vmware.ph.phservice.common.vmomi.client.AuthenticationHelper;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClientFactory;
import com.vmware.vim.vmomi.client.ext.RequestRetryCallback;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.client.http.impl.HttpConfigurationImpl;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.Executor;

public class VmomiClientFactoryImpl implements VmomiClientFactory {
  private final Class<?> _versionClass;
  
  private final VmodlContext _vmodlContext;
  
  private final Executor _threadPool;
  
  private KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private String _namespace;
  
  private Integer _timeoutMs;
  
  private Integer _maxConnections;
  
  private RequestRetryCallback _requestRetryCallback;
  
  public VmomiClientFactoryImpl(Class<?> versionClass, VmodlContext vmodlContext, Executor threadPool) {
    this._versionClass = Objects.<Class<?>>requireNonNull(versionClass);
    this._vmodlContext = Objects.<VmodlContext>requireNonNull(vmodlContext);
    this._threadPool = Objects.<Executor>requireNonNull(threadPool);
  }
  
  public void setTrustStore(KeyStore trustStore) {
    this._trustStore = trustStore;
  }
  
  public void setThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this._thumbprintVerifier = thumbprintVerifier;
  }
  
  public void setNamespace(String namespace) {
    this._namespace = namespace;
  }
  
  public void setTimeoutMs(Integer timeoutMs) {
    this._timeoutMs = timeoutMs;
  }
  
  public void setMaxConnections(Integer maxConnections) {
    this._maxConnections = maxConnections;
  }
  
  public void setRequestRetryCallback(RequestRetryCallback requestRetryCallback) {
    this._requestRetryCallback = requestRetryCallback;
  }
  
  public VmomiClient create(URI sdkEndpoint) {
    return create(sdkEndpoint, null);
  }
  
  public VmomiClient create(URI sdkEndpoint, AuthenticationHelper authenticationHelper) {
    HttpClientConfiguration httpClientConfig = createHttpConfiguration();
    VmomiClient vmodlClient = new VmomiClientImpl(sdkEndpoint, this._versionClass, httpClientConfig, this._vmodlContext, this._namespace, authenticationHelper);
    return vmodlClient;
  }
  
  private HttpClientConfiguration createHttpConfiguration() {
    HttpConfigurationImpl httpConfigurationImpl = new HttpConfigurationImpl();
    httpConfigurationImpl.setTrustStore(this._trustStore);
    httpConfigurationImpl.setThumbprintVerifier(this._thumbprintVerifier);
    httpConfigurationImpl.setCheckStaleConnection(true);
    if (this._timeoutMs != null)
      httpConfigurationImpl.setTimeoutMs(this._timeoutMs.intValue()); 
    if (this._maxConnections != null)
      httpConfigurationImpl.setMaxConnections(this._maxConnections.intValue()); 
    HttpClientConfiguration httpClientConfig = HttpClientConfiguration.Factory.newInstance();
    httpClientConfig.setExecutor(this._threadPool);
    httpClientConfig.setHttpConfiguration((HttpConfiguration)httpConfigurationImpl);
    httpClientConfig.setRequestRetryCallback(this._requestRetryCallback);
    return httpClientConfig;
  }
}
