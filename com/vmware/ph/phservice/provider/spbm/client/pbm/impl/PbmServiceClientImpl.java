package com.vmware.ph.phservice.provider.spbm.client.pbm.impl;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.vim.binding.pbm.ServiceInstance;
import com.vmware.vim.binding.pbm.ServiceInstanceContent;
import com.vmware.vim.binding.pbm.profile.ProfileManager;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import com.vmware.vim.vmomi.core.types.VmodlVersionMap;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class PbmServiceClientImpl implements PbmServiceClient {
  private final XServiceClientContext _xServiceClientContext;
  
  private final VmodlContext vmodlContext;
  
  private final VcClient vcClient;
  
  private final Client vmomiClient;
  
  private final Class<?> vmodlVersion;
  
  private final Object lock = new Object();
  
  private ServiceInstanceContent serviceInstanceContent;
  
  public PbmServiceClientImpl(Client vmomiClient, XServiceClientContext xServiceClientContext) {
    this._xServiceClientContext = xServiceClientContext;
    this.vmodlContext = VmodlContext.getContext();
    this.vcClient = this._xServiceClientContext.getVcClient();
    this.vmomiClient = vmomiClient;
    this.vmodlVersion = this._xServiceClientContext.getxClientVmodlVersion();
  }
  
  public URI getServiceUri() {
    return this.vmomiClient.getBinding().getEndpointUri();
  }
  
  public Client getVmomiClient() {
    return this.vmomiClient;
  }
  
  public VmodlVersion getVmodlVersion() {
    return VmodlVersionMap.Factory.getVmodlVersionMap().getVersion(this.vmodlVersion);
  }
  
  public ServiceInstanceContent getServiceInstanceContent() throws InterruptedException, ExecutionException {
    synchronized (this.lock) {
      if (this.serviceInstanceContent == null) {
        ManagedObjectReference pbmServiceInstanceMoRef = new ManagedObjectReference("PbmServiceInstance", "ServiceInstance");
        ServiceInstance pbmServiceInstance = createStub(pbmServiceInstanceMoRef);
        BlockingFuture blockingFuture = new BlockingFuture();
        pbmServiceInstance.retrieveContent((Future)blockingFuture);
        this.serviceInstanceContent = (ServiceInstanceContent)blockingFuture.get();
        return this.serviceInstanceContent;
      } 
    } 
    return this.serviceInstanceContent;
  }
  
  public void close() {
    if (getXServiceClientContext().getExecutor() != null) {
      ExecutorService service = (ExecutorService)getXServiceClientContext().getExecutor();
      service.shutdownNow();
    } 
    if (this.vmomiClient != null)
      this.vmomiClient.shutdown(); 
  }
  
  public <T extends ManagedObject> T createStub(ManagedObjectReference moRef) {
    ManagedObject managedObject1;
    RequestContextImpl requestContextImpl;
    VmodlType vmodlType = this.vmodlContext.getVmodlTypeMap().getVmodlType(moRef.getType());
    Class<T> typeClass = vmodlType.getTypeClass();
    T managedObject = null;
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      managedObject1 = this.vmomiClient.createStub(typeClass, moRef);
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
    RequestContext reqContext = ((Stub)managedObject1)._getRequestContext();
    if (reqContext == null)
      requestContextImpl = new RequestContextImpl(); 
    if (this.vmomiClient.getBinding().getSession() != null)
      requestContextImpl.put("vcSessionCookie", this.vmomiClient.getBinding().getSession().getId()); 
    ((Stub)managedObject1)._setRequestContext((RequestContext)requestContextImpl);
    return (T)managedObject1;
  }
  
  public ProfileManager getProfileManager() throws InterruptedException, ExecutionException {
    ProfileManager profileManager = createStub(getServiceInstanceContent().getProfileManager());
    return profileManager;
  }
  
  public VmodlContext getVmodlContext() {
    return this.vmodlContext;
  }
  
  public VcClient getVcClient() {
    return this.vcClient;
  }
  
  public XServiceClientContext getXServiceClientContext() {
    return this._xServiceClientContext;
  }
}
