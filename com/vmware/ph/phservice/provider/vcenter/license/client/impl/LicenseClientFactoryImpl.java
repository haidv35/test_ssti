package com.vmware.ph.phservice.provider.vcenter.license.client.impl;

import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClient;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClientFactory;
import com.vmware.vim.vmomi.client.ext.InvocationInterceptor;
import com.vmware.vim.vmomi.client.ext.ResultInterceptor;
import com.vmware.vim.vmomi.client.ext.ServerEndpointProvider;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import java.net.URI;
import java.util.concurrent.Executor;

public class LicenseClientFactoryImpl implements LicenseClientFactory {
  private final Class<?> _versionClass;
  
  private final Executor _threadPool;
  
  private LicenseClientFactory.LicenseClientAutomaticAuthenticator _clientAuthenticator;
  
  private InvocationInterceptor _invocationInterceptor;
  
  private ResultInterceptor _resultInterceptor;
  
  public LicenseClientFactoryImpl(Class<?> versionClass, Executor threadPool) {
    this._versionClass = versionClass;
    this._threadPool = threadPool;
  }
  
  public void setClientAuthenticator(LicenseClientFactory.LicenseClientAutomaticAuthenticator clientAuthenticator) {
    this._clientAuthenticator = clientAuthenticator;
  }
  
  public void setInvocationInterceptor(InvocationInterceptor invocationInterceptor) {
    this._invocationInterceptor = invocationInterceptor;
  }
  
  public void setResultInterceptor(ResultInterceptor resultInterceptor) {
    this._resultInterceptor = resultInterceptor;
  }
  
  public final LicenseClient createClient(URI licenseUri, HttpConfiguration httpConfig) {
    LicenseClientImpl client = new LicenseClientImpl(this._versionClass, this._threadPool, licenseUri, httpConfig, this._clientAuthenticator, this._invocationInterceptor, this._resultInterceptor);
    return client;
  }
  
  public LicenseClient createClient(ServerEndpointProvider licenseUriProvider, HttpConfiguration httpConfig) {
    LicenseClientImpl client = new LicenseClientImpl(this._versionClass, this._threadPool, licenseUriProvider, httpConfig, this._clientAuthenticator, this._invocationInterceptor, this._resultInterceptor);
    return client;
  }
}
