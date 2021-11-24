package com.vmware.ph.phservice.provider.common.vmomi.mo;

import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import java.util.List;

public interface MoTypesProvider {
  List<ManagedObjectType> getManagedObjectTypes();
}
