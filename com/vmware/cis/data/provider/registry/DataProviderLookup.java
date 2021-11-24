package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;

public interface DataProviderLookup {
  Collection<DataProvider> getProviders();
}
