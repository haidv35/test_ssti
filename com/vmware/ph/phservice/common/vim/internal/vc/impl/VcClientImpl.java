package com.vmware.ph.phservice.common.vim.internal.vc.impl;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vim.internal.vc.LoginHelper;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.binding.vim.InternalServiceInstanceContent;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vim.UserSession;
import com.vmware.vim.binding.vim.fault.InvalidLocale;
import com.vmware.vim.binding.vim.fault.InvalidLogin;
import com.vmware.vim.binding.vim.fault.NotAuthenticated;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import com.vmware.vim.vmomi.core.types.VmomiService;
import com.vmware.vim.vmomi.core.types.impl.VmomiServiceImpl;
import java.net.URI;
import java.util.Optional;

public class VcClientImpl implements VcClient {
  private final Client _vmomiClient;
  
  private final Class<?> _versionClass;
  
  private final VmodlContext _vmodlContext;
  
  private final Optional<LoginHelper> _loginHelper;
  
  private VmomiService _vmomiService;
  
  private final Object _lock = new Object();
  
  private ServiceInstanceContent _serviceInstanceContent;
  
  private InternalServiceInstanceContent _internalServiceInstanceContent;
  
  VcClientImpl(Client vmomiClient, String namespace, Class<?> versionClass, VmodlContext vmodlContext) {
    this(vmomiClient, namespace, versionClass, vmodlContext, Optional.empty());
  }
  
  VcClientImpl(Client vmomiClient, String namespace, Class<?> versionClass, VmodlContext vmodlContext, Optional<LoginHelper> loginHelper) {
    this._vmomiClient = vmomiClient;
    this._versionClass = versionClass;
    if (namespace != null)
      this._vmomiService = (VmomiService)new VmomiServiceImpl("service-" + namespace, namespace); 
    this._vmodlContext = vmodlContext;
    this._loginHelper = loginHelper;
  }
  
  public Client getVlsiClient() {
    return this._vmomiClient;
  }
  
  public void close() {
    try {
      try {
        getSessionManager().logout();
      } catch (NotAuthenticated notAuthenticated) {}
    } finally {
      this._vmomiClient.shutdown();
    } 
  }
  
  public VmodlVersion getVmodlVersion() {
    return this._vmodlContext.getVmodlVersionMap().getVersion(this._versionClass);
  }
  
  public VmodlContext getVmodlContext() {
    return this._vmodlContext;
  }
  
  public URI getServiceUri() {
    return this._vmomiClient.getBinding().getEndpointUri();
  }
  
  public ServiceInstanceContent getServiceInstanceContent() {
    synchronized (this._lock) {
      if (this._serviceInstanceContent == null) {
        ServiceInstance serviceInstance = createServiceInstanceStub();
        this._serviceInstanceContent = serviceInstance.retrieveContent();
      } 
    } 
    return this._serviceInstanceContent;
  }
  
  public InternalServiceInstanceContent getInternalServiceInstanceContent() {
    synchronized (this._lock) {
      if (this._internalServiceInstanceContent == null) {
        ServiceInstance serviceInstance = createServiceInstanceStub();
        this._internalServiceInstanceContent = serviceInstance.retrieveInternalContent();
      } 
    } 
    return this._internalServiceInstanceContent;
  }
  
  public SessionManager getSessionManager() {
    SessionManager sessionManager = null;
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      sessionManager = (SessionManager)this._vmomiClient.createStub(SessionManager.class, 
          
          getServiceInstanceContent().getSessionManager());
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
    return sessionManager;
  }
  
  public <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createMo(ManagedObjectReference moRef) {
    VmodlType vmodlType = null;
    if (this._vmomiService != null) {
      vmodlType = this._vmodlContext.getVmodlTypeMap().getVmodlType(moRef
          .getType(), this._vmomiService);
    } else {
      vmodlType = this._vmodlContext.getVmodlTypeMap().getVmodlType(moRef.getType());
    } 
    Class<T> typeClass = vmodlType.getTypeClass();
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      return (T)this._vmomiClient.createStub(typeClass, moRef);
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
  
  public Optional<UserSession> login() throws InvalidLogin, InvalidLocale, SsoTokenProviderException {
    if (this._loginHelper.isPresent())
      return Optional.of(((LoginHelper)this._loginHelper.get()).login(getSessionManager())); 
    return super.login();
  }
  
  private ServiceInstance createServiceInstanceStub() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      return (ServiceInstance)this._vmomiClient.createStub(ServiceInstance.class, VimVmodlUtil.SERVICE_INSTANCE_MOREF);
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
}
