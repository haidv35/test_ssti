package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.provider.DataProvider;

public interface DataProviderConnection extends AutoCloseable {
  DataProvider getDataProvider();
}
