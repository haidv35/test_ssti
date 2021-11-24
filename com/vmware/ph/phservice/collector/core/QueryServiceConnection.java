package com.vmware.ph.phservice.collector.core;

import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;

public interface QueryServiceConnection extends AutoCloseable {
  QueryService getQueryService();
  
  ContextFactory getContextFactory();
  
  void close();
}
