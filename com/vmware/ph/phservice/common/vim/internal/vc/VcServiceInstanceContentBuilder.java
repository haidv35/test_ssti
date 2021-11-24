package com.vmware.ph.phservice.common.vim.internal.vc;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.binding.vim.ServiceInstanceContent;

public class VcServiceInstanceContentBuilder implements Builder<ServiceInstanceContent> {
  private final VcClient _vcClient;
  
  public VcServiceInstanceContentBuilder(VcClient vcClient) {
    this._vcClient = vcClient;
  }
  
  public ServiceInstanceContent build() {
    return this._vcClient.getServiceInstanceContent();
  }
}
