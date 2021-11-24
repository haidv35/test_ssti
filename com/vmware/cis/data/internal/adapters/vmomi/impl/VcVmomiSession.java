package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vim.fault.InvalidLocale;
import com.vmware.vim.binding.vim.fault.InvalidLogin;
import com.vmware.vim.binding.vim.fault.NotAuthenticated;
import com.vmware.vim.binding.vim.version.internal.version8;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import com.vmware.vim.vmomi.core.security.SignInfo;
import com.vmware.vim.vmomi.core.security.impl.SignInfoImpl;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VcVmomiSession {
  private static Logger logger = LoggerFactory.getLogger(VcVmomiSession.class);
  
  private final URI _serviceUrl;
  
  private final Client _vlsiClient;
  
  private final SessionManager _sessionManager;
  
  static VcVmomiSession createSession(VmodlContext vmodlContext, URI vmomiUri, HttpConfiguration vlsiHttpConfig, AuthenticationTokenSource tokenSource) {
    SessionManager sessionManager;
    Client vlsiClient = createVlsiClient(vmodlContext, vmomiUri, vlsiHttpConfig);
    try {
      sessionManager = getSessionManager(vlsiClient);
      loginByToken(sessionManager, tokenSource);
    } catch (RuntimeException ex) {
      vlsiClient.shutdown();
      throw ex;
    } 
    return new VcVmomiSession(vmomiUri, vlsiClient, sessionManager);
  }
  
  private VcVmomiSession(URI serviceUrl, Client vlsiClient, SessionManager sessionManager) {
    assert vlsiClient != null;
    assert sessionManager != null;
    this._serviceUrl = serviceUrl;
    this._vlsiClient = vlsiClient;
    this._sessionManager = sessionManager;
    logger.debug("Logged in into vCenter at: {}", serviceUrl);
  }
  
  public String getSessionCookie() {
    return this._vlsiClient.getBinding().getSession().getId();
  }
  
  public void logout() {
    try {
      this._sessionManager.logout();
      logger.debug("Logged out of vCenter at: {}", this._serviceUrl);
    } catch (NotAuthenticated notAuthenticated) {
    
    } catch (RuntimeException ex) {
      logger.warn("Error while logging out of VC", ex);
    } 
    try {
      this._vlsiClient.shutdown();
    } catch (RuntimeException ex) {
      logger.warn("Error while shutting down VLSI client", ex);
    } 
  }
  
  private static Client createVlsiClient(VmodlContext vmodlContext, URI vmomiUri, HttpConfiguration vlsiHttpConfig) {
    HttpClientConfiguration clientConfig = HttpClientConfiguration.Factory.newInstance();
    clientConfig.setHttpConfiguration(vlsiHttpConfig);
    return Client.Factory.createClient(vmomiUri, version8.class, vmodlContext, (ClientConfiguration)clientConfig);
  }
  
  private static ServiceInstance getServiceInstance(Client vlsiClient) {
    ManagedObjectReference ref = new ManagedObjectReference("ServiceInstance", "ServiceInstance", null);
    return VlsiClientUtil.<ServiceInstance>createStub(vlsiClient, ServiceInstance.class, ref);
  }
  
  private static SessionManager getSessionManager(Client vlsiClient) {
    ServiceInstance si = getServiceInstance(vlsiClient);
    ManagedObjectReference ref = si.retrieveContent().getSessionManager();
    return VlsiClientUtil.<SessionManager>createStub(vlsiClient, SessionManager.class, ref);
  }
  
  private static void loginByToken(SessionManager sessionManager, AuthenticationTokenSource authn) {
    try {
      SignInfoImpl signInfoImpl = new SignInfoImpl(authn.getConfirmationKey(), authn.getAuthenticationToken());
      RequestContextImpl rc = getRequestContext((ManagedObject)sessionManager);
      rc.setSignInfo((SignInfo)signInfoImpl);
      sessionManager.loginByToken(null);
      rc.setSignInfo(null);
    } catch (InvalidLogin ex) {
      throw new RuntimeException(ex);
    } catch (InvalidLocale ex) {
      throw new RuntimeException(ex);
    } 
  }
  
  private static RequestContextImpl getRequestContext(ManagedObject mo) {
    Stub stub = (Stub)mo;
    RequestContextImpl rc = (RequestContextImpl)stub._getRequestContext();
    if (rc == null) {
      rc = new RequestContextImpl();
      stub._setRequestContext((RequestContext)rc);
    } 
    return rc;
  }
}
