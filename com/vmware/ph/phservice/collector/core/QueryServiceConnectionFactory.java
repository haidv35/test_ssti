package com.vmware.ph.phservice.collector.core;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;

public interface QueryServiceConnectionFactory {
  QueryServiceConnection createQueryServiceConnection(Builder<DataProvidersConnection> paramBuilder);
}
