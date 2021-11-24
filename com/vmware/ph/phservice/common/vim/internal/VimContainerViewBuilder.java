package com.vmware.ph.phservice.common.vim.internal;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vim.view.ViewManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;

public class VimContainerViewBuilder implements Builder<ManagedObjectReference> {
  private final Client _vimVlsiClient;
  
  public VimContainerViewBuilder(Client vimVlsiClient) {
    this._vimVlsiClient = vimVlsiClient;
  }
  
  public ManagedObjectReference build() {
    ServiceInstanceContent serviceInstanceContent = (new VimServiceInstanceContentBuilder(this._vimVlsiClient)).build();
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      ViewManager viewManager = (ViewManager)this._vimVlsiClient.createStub(ViewManager.class, serviceInstanceContent.viewManager);
      ManagedObjectReference containerViewMoRef = viewManager.createContainerView(serviceInstanceContent
          .getRootFolder(), null, true);
      return containerViewMoRef;
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
  
  public void destroy(ManagedObjectReference viewMoRef) {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      ContainerView containerView = (ContainerView)this._vimVlsiClient.createStub(ContainerView.class, viewMoRef);
      containerView.destroy();
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
}
