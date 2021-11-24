package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.vcenter.VcenterDataProviderFactory;

public interface VcenterDataProviderFactoryRegistry {
  void register(VcenterDataProviderFactory paramVcenterDataProviderFactory);
  
  void unregister(VcenterDataProviderFactory paramVcenterDataProviderFactory);
}
