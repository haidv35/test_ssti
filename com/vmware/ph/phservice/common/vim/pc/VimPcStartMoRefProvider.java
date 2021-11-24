package com.vmware.ph.phservice.common.vim.pc;

import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vim.internal.VimContainerViewBuilder;
import com.vmware.ph.phservice.common.vmomi.pc.PcStartMoRefProvider;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.view.ContainerView;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.types.VmodlType;

public class VimPcStartMoRefProvider implements PcStartMoRefProvider {
  private final Client _vimVlsiClient;
  
  public VimPcStartMoRefProvider(Client vimVlsiClient) {
    this._vimVlsiClient = vimVlsiClient;
  }
  
  public ManagedObjectReference getStartMoRef(VmodlType pcStartVmodlType) {
    if (pcStartVmodlType.getTypeClass().equals(ServiceInstance.class))
      return VimVmodlUtil.SERVICE_INSTANCE_MOREF; 
    if (pcStartVmodlType.getTypeClass().equals(ContainerView.class))
      return (new VimContainerViewBuilder(this._vimVlsiClient)).build(); 
    return null;
  }
  
  public void destroyStartMoRefIfNeeded(ManagedObjectReference startMoRef) {
    if (ContainerView.class.getSimpleName().equals(startMoRef.getType())) {
      VimContainerViewBuilder vimContainerViewBuilder = new VimContainerViewBuilder(this._vimVlsiClient);
      vimContainerViewBuilder.destroy(startMoRef);
    } 
  }
}
