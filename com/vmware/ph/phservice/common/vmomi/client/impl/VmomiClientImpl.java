package com.vmware.ph.phservice.common.vmomi.client.impl;

import com.vmware.ph.phservice.common.vmomi.client.AuthenticationHelper;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import com.vmware.vim.vmomi.core.types.VmomiService;
import com.vmware.vim.vmomi.core.types.impl.VmomiServiceImpl;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Objects;

public class VmomiClientImpl implements VmomiClient {
  private final Class<?> _versionClass;
  
  private final VmodlContext _vmodlContext;
  
  private final AuthenticationHelper _authenticationHelper;
  
  private final VmomiService _vmomiService;
  
  private final Client _vlsiClient;
  
  public VmomiClientImpl(URI endpointUri, Class<?> versionClass, HttpClientConfiguration httpClientConfiguration, VmodlContext vmodlContext) {
    this(endpointUri, versionClass, httpClientConfiguration, vmodlContext, null);
  }
  
  public VmomiClientImpl(URI endpointUri, Class<?> versionClass, HttpClientConfiguration httpClientConfiguration, VmodlContext vmodlContext, String namespace) {
    this(endpointUri, versionClass, httpClientConfiguration, vmodlContext, namespace, null);
  }
  
  public VmomiClientImpl(URI endpointUri, Class<?> versionClass, HttpClientConfiguration httpClientConfiguration, VmodlContext vmodlContext, String namespace, AuthenticationHelper authenticationHelper) {
    Objects.requireNonNull(endpointUri);
    this._versionClass = Objects.<Class<?>>requireNonNull(versionClass);
    Objects.requireNonNull(httpClientConfiguration);
    this._vmodlContext = Objects.<VmodlContext>requireNonNull(vmodlContext);
    if (namespace != null) {
      this._vmomiService = (VmomiService)new VmomiServiceImpl("service-" + namespace, namespace);
    } else {
      this._vmomiService = null;
    } 
    this
      ._vlsiClient = Client.Factory.createClient(endpointUri, versionClass, vmodlContext, (ClientConfiguration)httpClientConfiguration);
    this._authenticationHelper = authenticationHelper;
  }
  
  public Client getVlsiClient() {
    return this._vlsiClient;
  }
  
  public void close() {
    this._vlsiClient.shutdown();
  }
  
  public VmodlVersion getVmodlVersion() {
    return this._vmodlContext.getVmodlVersionMap().getVersion(this._versionClass);
  }
  
  public VmodlContext getVmodlContext() {
    return this._vmodlContext;
  }
  
  public URI getServiceUri() {
    return this._vlsiClient.getBinding().getEndpointUri();
  }
  
  public <T extends ManagedObject> T createStub(ManagedObjectReference moRef) throws Exception {
    VmodlType vmodlType = null;
    if (this._vmomiService != null) {
      vmodlType = this._vmodlContext.getVmodlTypeMap().getVmodlType(moRef
          .getType(), this._vmomiService);
    } else {
      vmodlType = this._vmodlContext.getVmodlTypeMap().getVmodlType(moRef.getType());
    } 
    Class<T> typeClass = vmodlType.getTypeClass();
    T mo = null;
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      managedObject = this._vlsiClient.createStub(typeClass, moRef);
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
    ManagedObject managedObject = decorateStubWithAuthentication(this, managedObject, typeClass, this._authenticationHelper);
    return (T)managedObject;
  }
  
  private static <T extends ManagedObject> T decorateStubWithAuthentication(VmomiClient vmomiClient, T originalStub, Class<T> stubTypeClass, AuthenticationHelper authenticationHelper) {
    ManagedObject managedObject;
    T decoratedStub = originalStub;
    if (authenticationHelper != null) {
      InvocationHandler stubInvocationHandler = new AuthenicatedStubInvocationHandler((Stub)originalStub, vmomiClient, authenticationHelper);
      managedObject = (ManagedObject)Proxy.newProxyInstance(stubTypeClass
          .getClassLoader(), new Class[] { stubTypeClass }, stubInvocationHandler);
    } 
    return (T)managedObject;
  }
  
  private static class AuthenicatedStubInvocationHandler implements InvocationHandler {
    private final Stub _stub;
    
    private final VmomiClient _vmomiClient;
    
    private final AuthenticationHelper _authenticationHelper;
    
    public AuthenicatedStubInvocationHandler(Stub stub, VmomiClient vmomiClient, AuthenticationHelper authenticationHelper) {
      this._stub = Objects.<Stub>requireNonNull(stub);
      this._vmomiClient = vmomiClient;
      this._authenticationHelper = Objects.<AuthenticationHelper>requireNonNull(authenticationHelper);
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      this._authenticationHelper.addAuthenticationContext(this._stub, this._vmomiClient);
      Object invoationResult = method.invoke(this._stub, args);
      return invoationResult;
    }
  }
}
