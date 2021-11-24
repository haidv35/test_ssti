package com.vmware.ph.phservice.common.vmomi.pc;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlType;

public interface PcStartMoRefProvider {
  ManagedObjectReference getStartMoRef(VmodlType paramVmodlType);
  
  void destroyStartMoRefIfNeeded(ManagedObjectReference paramManagedObjectReference);
}
