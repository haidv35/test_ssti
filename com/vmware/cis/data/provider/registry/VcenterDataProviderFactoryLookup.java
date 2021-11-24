package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.vcenter.VcenterDataProviderFactory;
import java.util.Collection;

public interface VcenterDataProviderFactoryLookup {
  Collection<VcenterDataProviderFactory> get();
}
