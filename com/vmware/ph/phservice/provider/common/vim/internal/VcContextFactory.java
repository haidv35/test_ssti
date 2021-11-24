package com.vmware.ph.phservice.provider.common.vim.internal;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.provider.common.internal.Context;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.binding.vim.Capability;
import com.vmware.vim.binding.vim.ServiceInstance;

public class VcContextFactory implements ContextFactory {
  static final String VC_ID_NAME = "global-vcId";
  
  static final String SERVICE_INSTANCE_CAPABILITY_NAME = "SI-Capability";
  
  static final String SERVICE_INSTANCE_ABOUT_INFO_NAME = "SI-About";
  
  private VcClientProvider _vcClientProvider;
  
  private Builder<VcClient> _vcClientBuilder;
  
  public VcContextFactory(VcClientProvider vcClientProvider) {
    this._vcClientProvider = vcClientProvider;
  }
  
  public VcContextFactory(Builder<VcClient> vcClientBuilder) {
    this._vcClientBuilder = vcClientBuilder;
  }
  
  public Context createContext(String collectorId, String collectorInstanceId, String collectionId) {
    VcClient vcClient = null;
    boolean closeClient = false;
    if (this._vcClientProvider != null) {
      vcClient = this._vcClientProvider.getVcClient();
    } else {
      vcClient = this._vcClientBuilder.build();
      closeClient = true;
    } 
    try {
      ServiceInstance serviceInstance = getServiceInstance(vcClient);
      AboutInfo aboutInfo = serviceInstance.getContent().getAbout();
      String vcGuid = aboutInfo.getInstanceUuid();
      Capability capability = serviceInstance.getCapability();
      Context context = new Context(collectorId, collectorInstanceId, collectionId);
      context.put("global-vcId", vcGuid.toUpperCase());
      context.put("SI-Capability", capability);
      context.put("SI-About", aboutInfo);
      return context;
    } finally {
      if (closeClient)
        vcClient.close(); 
    } 
  }
  
  private ServiceInstance getServiceInstance(VcClient vcClient) {
    return vcClient.<ServiceInstance>createMo(VimVmodlUtil.SERVICE_INSTANCE_MOREF);
  }
}
