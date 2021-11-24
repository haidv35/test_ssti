package com.vmware.ph.phservice.common.vim.pc;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.internal.VimContainerViewBuilder;
import com.vmware.ph.phservice.common.vim.internal.VimServiceInstanceContentBuilder;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.client.Client;

public class VimPropertyCollectorReader extends PropertyCollectorReader {
  private final Client _vimVlsiClient;
  
  public VimPropertyCollectorReader(Client vimVlsiClient) {
    super(new VimPropertyCollectorBuilder(vimVlsiClient), new VimServerGuidBuilder(vimVlsiClient));
    this._vimVlsiClient = vimVlsiClient;
  }
  
  public ManagedObjectReference createContainerView() {
    return (new VimContainerViewBuilder(this._vimVlsiClient)).build();
  }
  
  public void destroyContainerView(ManagedObjectReference containerViewMoRef) {
    VimContainerViewBuilder vimContainerViewBuilder = new VimContainerViewBuilder(this._vimVlsiClient);
    vimContainerViewBuilder.destroy(containerViewMoRef);
  }
  
  private static class VimPropertyCollectorBuilder implements Builder<PropertyCollector> {
    private final Client _vimVlsiClient;
    
    public VimPropertyCollectorBuilder(Client vimVlsiClient) {
      this._vimVlsiClient = vimVlsiClient;
    }
    
    public PropertyCollector build() {
      ServiceInstanceContent serviceInstanceContent = VimPropertyCollectorReader.getVimServiceInstanceContent(this._vimVlsiClient);
      ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        PropertyCollector propertyCollector = (PropertyCollector)this._vimVlsiClient.createStub(PropertyCollector.class, serviceInstanceContent.propertyCollector);
        return propertyCollector;
      } finally {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
      } 
    }
  }
  
  private static class VimServerGuidBuilder implements Builder<String> {
    private final Client _vimVlsiClient;
    
    public VimServerGuidBuilder(Client vimVlsiClient) {
      this._vimVlsiClient = vimVlsiClient;
    }
    
    public String build() {
      ServiceInstanceContent serviceInstanceContent = VimPropertyCollectorReader.getVimServiceInstanceContent(this._vimVlsiClient);
      String serverGuid = serviceInstanceContent.getAbout().getInstanceUuid();
      return serverGuid;
    }
  }
  
  private static ServiceInstanceContent getVimServiceInstanceContent(Client vimVlsiClient) {
    return (new VimServiceInstanceContentBuilder(vimVlsiClient)).build();
  }
}
