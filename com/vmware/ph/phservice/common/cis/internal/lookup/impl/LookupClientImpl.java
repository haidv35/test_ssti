package com.vmware.ph.phservice.common.cis.internal.lookup.impl;

import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.vim.binding.lookup.LookupService;
import com.vmware.vim.binding.lookup.ServiceContent;
import com.vmware.vim.binding.lookup.ServiceInstance;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.version.version2;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;

public class LookupClientImpl implements LookupClient {
  private static final ManagedObjectReference LS_SERVICE_INSTANCE_MO_REF = new ManagedObjectReference("LookupServiceInstance", "ServiceInstance");
  
  private final Class<?> _versionClass;
  
  private final URI _lsUri;
  
  private final HttpClientConfiguration _httpClientConfig;
  
  private final VmodlContext _vmodlContext;
  
  private final Client _vmomiClient;
  
  private final boolean _isServiceRegistrationSupported;
  
  private ServiceContent _serviceContent;
  
  private LookupService _lookupService;
  
  private ServiceRegistration _serviceRegistration;
  
  public LookupClientImpl(URI lsUri, Class<?> versionClass, HttpClientConfiguration httpClientConfig, VmodlContext vmodlContext) {
    this._versionClass = versionClass;
    this._lsUri = lsUri;
    this._httpClientConfig = httpClientConfig;
    this._vmodlContext = vmodlContext;
    this
      ._vmomiClient = Client.Factory.createClient(this._lsUri, this._versionClass, vmodlContext, (ClientConfiguration)this._httpClientConfig);
    VmodlVersion version2 = vmodlContext.getVmodlVersionMap().getVersion(version2.class);
    this
      ._isServiceRegistrationSupported = vmodlContext.getVmodlVersionMap().getVersion(this._versionClass).isCompatible(version2);
  }
  
  public VmodlContext getVmodlContext() {
    return this._vmodlContext;
  }
  
  public VmodlVersion getVmodlVersion() {
    return this._vmodlContext.getVmodlVersionMap().getVersion(this._versionClass);
  }
  
  public LookupService getLookupService() {
    if (this._lookupService == null) {
      ServiceContent sc = getServiceContent();
      this._lookupService = getManagedObjectInt(LookupService.class, sc
          
          .getLookupService());
    } 
    return this._lookupService;
  }
  
  public ServiceRegistration getServiceRegistration() {
    if (this._isServiceRegistrationSupported && this._serviceRegistration == null) {
      ServiceContent sc = getServiceContent();
      ManagedObjectReference srMoRef = sc.getServiceRegistration();
      if (srMoRef != null)
        this._serviceRegistration = getManagedObjectInt(ServiceRegistration.class, srMoRef); 
    } 
    return this._serviceRegistration;
  }
  
  public boolean isServiceRegistrationSupported() {
    return this._isServiceRegistrationSupported;
  }
  
  public void close() {
    if (this._vmomiClient != null)
      this._vmomiClient.shutdown(); 
  }
  
  private ServiceContent getServiceContent() {
    if (this._serviceContent == null) {
      ServiceInstance si = getManagedObjectInt(ServiceInstance.class, LS_SERVICE_INSTANCE_MO_REF);
      this._serviceContent = si.retrieveServiceContent();
    } 
    return this._serviceContent;
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
}
