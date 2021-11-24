package com.vmware.ph.phservice.provider.vcenter.license.client.impl;

import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClient;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClientException;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClientFactory;
import com.vmware.vim.binding.cis.license.SessionManagementService;
import com.vmware.vim.binding.cis.license.management.AssetManagementService;
import com.vmware.vim.binding.cis.license.management.SystemManagementService;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.sso.client.SamlToken;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.common.impl.ClientFutureImpl;
import com.vmware.vim.vmomi.client.ext.InvocationContext;
import com.vmware.vim.vmomi.client.ext.InvocationInterceptor;
import com.vmware.vim.vmomi.client.ext.RequestRetryCallback;
import com.vmware.vim.vmomi.client.ext.ResultInterceptor;
import com.vmware.vim.vmomi.client.ext.ServerEndpointProvider;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import com.vmware.vim.vmomi.core.security.SignInfo;
import com.vmware.vim.vmomi.core.security.impl.SignInfoImpl;
import java.net.URI;
import java.security.PrivateKey;
import java.util.concurrent.Executor;

public class LicenseClientImpl implements LicenseClient {
  private static final String ASSET_MNGMT_SVC_MO_REF_TYPE = "CisLicenseManagementAssetManagementService";
  
  private static final String ASSET_MNGMT_SVC_MO_REF_ID = "cis.license.management.AssetManagementService";
  
  private static final String SYSTEM_MNGMT_SVC_MO_REF_TYPE = "CisLicenseManagementSystemManagementService";
  
  private static final String SYSTEM_MNGMT_SVC_MO_REF_ID = "cis.license.management.SystemManagementService";
  
  private static final String SESSION_MNGMT_SVC_MO_REF_TYPE = "CisLicenseSessionManagementService";
  
  private static final String SESSION_MNGMT_SVC_MO_REF_ID = "cis.license.SessionManagementService";
  
  static final String LS_ADMIN_GROUP = "LicenseService.Administrators";
  
  private final Class<?> _versionClass;
  
  private final Executor _threadPool;
  
  private final HttpConfiguration _httpConfig;
  
  private final LicenseClientFactory.LicenseClientAutomaticAuthenticator _clientAuthenticator;
  
  private final InvocationInterceptor _invocationInterceptor;
  
  private final ResultInterceptor _resultInterceptor;
  
  private final URI _lsUri;
  
  private final ServerEndpointProvider _lsUriProvider;
  
  private Client _vmomiClient;
  
  private AssetManagementService _assetManagementService;
  
  private SystemManagementService _systemManagementService;
  
  private SessionManagementService _sessionManagementService;
  
  public LicenseClientImpl(Class<?> versionClass, Executor threadPool, URI lsUri, HttpConfiguration httpConfig, LicenseClientFactory.LicenseClientAutomaticAuthenticator clientAuthenticator, InvocationInterceptor invocationInterceptor, ResultInterceptor resultInterceptor) {
    this._versionClass = versionClass;
    this._threadPool = threadPool;
    this._lsUri = lsUri;
    this._lsUriProvider = null;
    this._httpConfig = httpConfig;
    this._clientAuthenticator = clientAuthenticator;
    this._invocationInterceptor = invocationInterceptor;
    this._resultInterceptor = resultInterceptor;
    initVmomiClient();
    initServices();
  }
  
  public LicenseClientImpl(Class<?> versionClass, Executor threadPool, ServerEndpointProvider lsUriProvider, HttpConfiguration httpConfig, LicenseClientFactory.LicenseClientAutomaticAuthenticator clientAuthenticator, InvocationInterceptor invocationInterceptor, ResultInterceptor resultInterceptor) {
    this._versionClass = versionClass;
    this._threadPool = threadPool;
    this._lsUri = null;
    this._lsUriProvider = lsUriProvider;
    this._httpConfig = httpConfig;
    this._clientAuthenticator = clientAuthenticator;
    this._invocationInterceptor = invocationInterceptor;
    this._resultInterceptor = resultInterceptor;
    initVmomiClient();
    initServices();
  }
  
  public AssetManagementService getAssetManagementService() {
    return this._assetManagementService;
  }
  
  public SystemManagementService getSystemManagementService() {
    return this._systemManagementService;
  }
  
  public <T extends com.vmware.vim.binding.vmodl.ManagedObject> T getManagedObject(Class<T> typeClass, ManagedObjectReference moRef) {
    return getManagedObjectInt(typeClass, moRef);
  }
  
  public void login(SamlToken token, PrivateKey privateKey) {
    RequestContextImpl ctx = new RequestContextImpl();
    SignInfoImpl authInfo = new SignInfoImpl(privateKey, token);
    ctx.setSignInfo((SignInfo)authInfo);
    ((Stub)this._sessionManagementService)._setRequestContext((RequestContext)ctx);
    ClientFutureImpl clientFutureImpl = new ClientFutureImpl();
    this._sessionManagementService.loginByToken((Future)clientFutureImpl);
    try {
      clientFutureImpl.get();
    } catch (Exception e) {
      throw new LicenseClientException(e);
    } 
  }
  
  public void logout() {
    ClientFutureImpl clientFutureImpl = new ClientFutureImpl();
    this._sessionManagementService.logout((Future)clientFutureImpl);
    try {
      clientFutureImpl.get();
    } catch (Exception e) {
      throw new LicenseClientException(e);
    } 
  }
  
  public void close() {
    this._assetManagementService = null;
    this._systemManagementService = null;
    this._sessionManagementService = null;
    this._vmomiClient.shutdown();
  }
  
  private void initVmomiClient() {
    HttpClientConfiguration clientConfig = HttpClientConfiguration.Factory.newInstance();
    clientConfig.setExecutor(this._threadPool);
    clientConfig.setHttpConfiguration(this._httpConfig);
    if (this._clientAuthenticator != null) {
      RequestRetryCallback requestRetryCallback = new AuthenticateRequestRetryCallbackImpl();
      clientConfig.setRequestRetryCallback(requestRetryCallback);
    } 
    clientConfig.setInvocationInterceptor(this._invocationInterceptor);
    clientConfig.setResultInterceptor(this._resultInterceptor);
    if (this._lsUriProvider != null) {
      clientConfig.setServerEndpointProvider(this._lsUriProvider);
      this
        ._vmomiClient = Client.Factory.createClient(null, this._versionClass, (ClientConfiguration)clientConfig);
    } else {
      this._vmomiClient = Client.Factory.createClient(this._lsUri, this._versionClass, (ClientConfiguration)clientConfig);
    } 
  }
  
  private void initServices() {
    ManagedObjectReference amsMoRef = new ManagedObjectReference("CisLicenseManagementAssetManagementService", "cis.license.management.AssetManagementService");
    this._assetManagementService = getManagedObjectInt(AssetManagementService.class, amsMoRef);
    ManagedObjectReference smsMoRef = new ManagedObjectReference("CisLicenseManagementSystemManagementService", "cis.license.management.SystemManagementService");
    this._systemManagementService = getManagedObjectInt(SystemManagementService.class, smsMoRef);
    ManagedObjectReference sessionMoRef = new ManagedObjectReference("CisLicenseSessionManagementService", "cis.license.SessionManagementService");
    this._sessionManagementService = getManagedObjectInt(SessionManagementService.class, sessionMoRef);
  }
  
  private <T extends com.vmware.vim.binding.vmodl.ManagedObject> T getManagedObjectInt(Class<T> typeClass, ManagedObjectReference moRef) {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      return (T)this._vmomiClient.createStub(typeClass, moRef);
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
  
  private class AuthenticateRequestRetryCallbackImpl implements RequestRetryCallback {
    private AuthenticateRequestRetryCallbackImpl() {}
    
    public boolean retry(Exception ex, InvocationContext context, int retryCount) {
      if (!(ex instanceof com.vmware.vim.binding.cis.license.fault.NotAuthenticatedFault))
        return false; 
      String methodName = context.getMethod().getName();
      if (methodName.equalsIgnoreCase("LoginByToken") || methodName
        .equalsIgnoreCase("Login") || methodName
        .equalsIgnoreCase("Logout"))
        return false; 
      if (retryCount != 0)
        return false; 
      try {
        LicenseClientImpl.this.logout();
      } catch (Exception exception) {}
      try {
        LicenseClientImpl.this._clientAuthenticator.login(LicenseClientImpl.this);
      } catch (Exception e) {
        return false;
      } 
      return true;
    }
  }
}
