package com.vmware.ph.phservice.provider.common;

import com.vmware.cis.data.provider.DataProvider;
import java.util.List;

public interface DataProvidersConnection extends AutoCloseable {
  List<DataProvider> getDataProviders() throws Exception;
  
  void close();
}
