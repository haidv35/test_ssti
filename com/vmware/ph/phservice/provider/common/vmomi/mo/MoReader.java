package com.vmware.ph.phservice.provider.common.vmomi.mo;

import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.vmomi.core.types.VmodlType;

public interface MoReader {
  ManagedObject getManagedObject(VmodlType paramVmodlType);
}
