package com.vmware.ph.phservice.cloud.dataapp.collector;

import com.vmware.ph.phservice.provider.common.DataProvidersConnection;

public interface DataProvidersConnectionFactory {
  DataProvidersConnection createDataProvidersConnection(Object paramObject);
}
