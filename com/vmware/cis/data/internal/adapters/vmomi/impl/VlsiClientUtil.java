package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.internal.adapters.vmomi.VmomiAuthenticator;
import com.vmware.cis.data.internal.adapters.vmomi.VmomiSession;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.common.ProtocolBinding;
import com.vmware.vim.vmomi.client.common.Session;
import com.vmware.vim.vmomi.client.ext.InvocationContext;
import com.vmware.vim.vmomi.client.ext.RequestRetryCallback;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VlsiClientUtil {
  private static Logger _logger = LoggerFactory.getLogger(VlsiClientUtil.class);
  
  public static final String VLSI_BINDING_PKG_DP = "com.vmware.vim.binding.cis.data.provider";
  
  public static final String VLSI_BINDING_PKG_VIM = "com.vmware.vim.binding.vim";
  
  public static <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createStub(Client vlsiClient, Class<T> stubBindingClass, ManagedObjectReference ref) {
    Validate.notNull(vlsiClient);
    Validate.notNull(stubBindingClass);
    Validate.notNull(ref);
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(VlsiClientUtil.class
        .getClassLoader());
    try {
      return (T)vlsiClient.createStub(stubBindingClass, ref);
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
  }
  
  public static VmodlContext createDefaultVmodlContext() {
    boolean lazyLoad = true;
    return createVmodlContext(new String[] { "com.vmware.vim.binding.cis.data.provider", "com.vmware.vim.binding.vim" }, lazyLoad);
  }
  
  public static VmodlContext createVmodlContext(String[] vlsiBindingPackages, boolean lazyLoad) {
    Validate.notEmpty((Object[])vlsiBindingPackages);
    Validate.noNullElements((Object[])vlsiBindingPackages);
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(VlsiClientUtil.class
        .getClassLoader());
    try {
      return VmodlContext.createContext(vlsiBindingPackages, lazyLoad);
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
  }
  
  public static Client createAuthenticatedVlsiClient(URI vmomiUri, HttpConfiguration httpConfiguration, VmodlContext vmodlContext, VmomiAuthenticator authenticator, AuthenticationTokenSource authenticationData, Class<?> version) {
    Validate.notNull(vmomiUri);
    Validate.notNull(httpConfiguration);
    Validate.notNull(vmodlContext);
    Validate.notNull(authenticator);
    Validate.notNull(authenticationData);
    Validate.notNull(version);
    HttpClientConfiguration clientConfiguration = HttpClientConfiguration.Factory.newInstance();
    clientConfiguration.setHttpConfiguration(httpConfiguration);
    VmomiSession vmomiSession = authenticator.login(authenticationData);
    if (vmomiSession != null) {
      ReloginRetryCallback userSession = new ReloginRetryCallback(vmomiSession, vmomiUri);
      clientConfiguration.setRequestRetryCallback(userSession);
    } 
    Client vlsiClient = Client.Factory.createClient(vmomiUri, version, vmodlContext, (ClientConfiguration)clientConfiguration);
    if (vmomiSession != null) {
      injectSession(vlsiClient.getBinding(), vmomiSession.getSessionCookie());
      return new AuthenticatedVlsiClient(vlsiClient, vmomiSession);
    } 
    return vlsiClient;
  }
  
  private static void injectSession(ProtocolBinding binding, String sessionCookie) {
    Session session = binding.createSession(sessionCookie);
    binding.setSession(session);
  }
  
  private static final class AuthenticatedVlsiClient implements Client {
    private final Client _vlsiClient;
    
    private final VmomiSession _vmomiSession;
    
    AuthenticatedVlsiClient(Client vlsiClient, VmomiSession vmomiSession) {
      this._vlsiClient = vlsiClient;
      this._vmomiSession = vmomiSession;
    }
    
    public <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createStub(Class<T> clazz, ManagedObjectReference moRef) {
      return (T)this._vlsiClient.createStub(clazz, moRef);
    }
    
    public <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createStub(Class<T> clazz, String moId) {
      return (T)this._vlsiClient.createStub(clazz, moId);
    }
    
    public ProtocolBinding getBinding() {
      return this._vlsiClient.getBinding();
    }
    
    public void shutdown() {
      this._vmomiSession.logout();
      this._vlsiClient.shutdown();
    }
  }
  
  private static final class ReloginRetryCallback implements RequestRetryCallback {
    private final VmomiSession _vmomiSession;
    
    private final URI _vmomiUri;
    
    ReloginRetryCallback(VmomiSession vmomiSession, URI vmomiUri) {
      this._vmomiSession = vmomiSession;
      this._vmomiUri = vmomiUri;
    }
    
    public boolean retry(Exception exception, InvocationContext context, int count) {
      if (!(exception instanceof com.vmware.vim.binding.vim.fault.NotAuthenticated) || count >= 1)
        return false; 
      VlsiClientUtil._logger.info("Recreating expired vmomi session for '{}'...", this._vmomiUri);
      ProtocolBinding binding = context.getBinding();
      String expiredCookie = binding.getSession().getId();
      String newCookie = this._vmomiSession.renewSessionCookie(expiredCookie);
      VlsiClientUtil.injectSession(binding, newCookie);
      VlsiClientUtil._logger.info("Recreated expired vmomi session for '{}'", this._vmomiUri);
      return true;
    }
  }
}
