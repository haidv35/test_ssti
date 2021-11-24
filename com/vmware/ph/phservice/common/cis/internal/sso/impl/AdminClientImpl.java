package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.AdminClient;
import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpoint;
import com.vmware.vim.binding.sso.admin.ConfigurationManagementService;
import com.vmware.vim.binding.sso.admin.ServiceContent;
import com.vmware.vim.binding.sso.admin.ServiceInstance;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.security.KeyStore;

public class AdminClientImpl implements AdminClient {
  private static final String SSO_ADMIN_SERVICE_INSTANCE = "SsoAdminServiceInstance";
  
  private final VmodlContext _vmodlContext;
  
  private final Class<?> _versionClass;
  
  private final SsoEndpoint _ssoAdminEndpoint;
  
  private final KeyStore _trustStore;
  
  private final ThumbprintVerifier _thumbprintVerifier;
  
  private final Object _configurationManagementServiceInitLock = new Object();
  
  private Client _vmomiClient;
  
  private ServiceInstance _serviceInstance;
  
  private ConfigurationManagementService _configurationManagementService;
  
  AdminClientImpl(VmodlContext vmodlContext, Class<?> versionClass, SsoEndpoint ssoAdminEndpoint, KeyStore trustStore, ThumbprintVerifier thumbprintVerifier) {
    this._vmodlContext = vmodlContext;
    this._versionClass = versionClass;
    this._ssoAdminEndpoint = ssoAdminEndpoint;
    this._trustStore = trustStore;
    this._thumbprintVerifier = thumbprintVerifier;
    initVmomiClient();
    initServiceInstance();
  }
  
  public ConfigurationManagementService getConfigurationManagementService() {
    if (this._configurationManagementService == null)
      initConfigurationManagementService(); 
    return this._configurationManagementService;
  }
  
  public void close() {
    this._vmomiClient.shutdown();
  }
  
  private void initVmomiClient() {
    HttpClientConfiguration httpClientConfig = createHttpClientConfiguration();
    this._vmomiClient = Client.Factory.createClient(this._ssoAdminEndpoint
        .getUrl(), this._versionClass, this._vmodlContext, (ClientConfiguration)httpClientConfig);
  }
  
  private void initServiceInstance() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      this._serviceInstance = (ServiceInstance)this._vmomiClient.createStub(ServiceInstance.class, "SsoAdminServiceInstance");
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
  
  private void initConfigurationManagementService() {
    synchronized (this._configurationManagementServiceInitLock) {
      if (this._configurationManagementService == null) {
        ServiceContent serviceContent = this._serviceInstance.retrieveServiceContent();
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(
              getClass().getClassLoader());
          this._configurationManagementService = (ConfigurationManagementService)this._vmomiClient.createStub(ConfigurationManagementService.class, serviceContent
              
              .getConfigurationManagementService());
        } finally {
          Thread.currentThread().setContextClassLoader(originalClassLoader);
        } 
      } 
    } 
  }
  
  private HttpClientConfiguration createHttpClientConfiguration() {
    HttpConfiguration httpConfig = HttpConfiguration.Factory.newInstance();
    httpConfig.setTrustStore(this._trustStore);
    httpConfig.setThumbprintVerifier(this._thumbprintVerifier);
    httpConfig.setTimeoutMs(180000);
    HttpClientConfiguration httpClientConfig = HttpClientConfiguration.Factory.newInstance();
    httpClientConfig.setHttpConfiguration(httpConfig);
    return httpClientConfig;
  }
}
