package com.vmware.cis.data.internal.provider.ext;

import com.vmware.cis.data.provider.DataProvider;

public interface ConnectionSupplier {
  DataProvider getConnection();
}
