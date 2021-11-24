package com.vmware.ph.phservice.common.vim.internal;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.vmomi.client.Client;

public class VimServiceInstanceContentBuilder implements Builder<ServiceInstanceContent> {
  private final Client _vimVlsiClient;
  
  public VimServiceInstanceContentBuilder(Client vimVlsiClient) {
    this._vimVlsiClient = vimVlsiClient;
  }
  
  public ServiceInstanceContent build() {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      ServiceInstance serviceInstance = (ServiceInstance)this._vimVlsiClient.createStub(ServiceInstance.class, VimVmodlUtil.SERVICE_INSTANCE_MOREF);
      return serviceInstance.getContent();
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
}
